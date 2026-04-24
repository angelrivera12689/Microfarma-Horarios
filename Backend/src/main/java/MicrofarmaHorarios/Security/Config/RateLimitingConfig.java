package MicrofarmaHorarios.Security.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration using Bucket4j
 * Previene brute force attacks y abuso de API
 * 
 * Límites:
 * - Login: 5 intentos por minuto
 * - API General: 100 peticiones por minuto por IP
 */
@Component
public class RateLimitingConfig implements HandlerInterceptor {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    // Límites por tipo de endpoint
    private static final int LOGIN_REQUESTS_PER_MINUTE = 5;
    private static final int GENERAL_REQUESTS_PER_MINUTE = 100;
    private static final int IMPORT_REQUESTS_PER_HOUR = 10;

    /**
     * Obtiene o crea un bucket de rate limiting para una IP
     */
    private Bucket resolveBucket(String ip, String endpoint) {
        String key = ip + ":" + endpoint;
        
        return cache.computeIfAbsent(key, k -> {
            Bandwidth limit;
            
            if (endpoint.contains("/auth/login")) {
                // Máximo 5 intentos de login por minuto
                limit = Bandwidth.classic(LOGIN_REQUESTS_PER_MINUTE, Refill.intervally(LOGIN_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
            } else if (endpoint.contains("/import")) {
                // Máximo 10 importaciones por hora
                limit = Bandwidth.classic(IMPORT_REQUESTS_PER_HOUR, Refill.intervally(IMPORT_REQUESTS_PER_HOUR, Duration.ofHours(1)));
            } else {
                // Límite general: 100 requests por minuto
                limit = Bandwidth.classic(GENERAL_REQUESTS_PER_MINUTE, Refill.intervally(GENERAL_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
            }
            
            return Bucket4j.builder()
                    .addLimit(limit)
                    .build();
        });
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        // Obtener IP del cliente (considerando proxies)
        String ip = getClientIpAddr(request);
        String endpoint = request.getRequestURI();
        
        // Obtener bucket para esta IP y endpoint
        Bucket bucket = resolveBucket(ip, endpoint);
        
        // Consumir un token
        if (bucket.tryConsume(1)) {
            // Request permitido
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            // Rate limit excedido
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\", \"retryAfter\": 60}");
            return false;
        }
    }

    /**
     * Obtiene la IP real del cliente (considerando proxies y load balancers)
     */
    private String getClientIpAddr(HttpServletRequest request) {
        String[] headerValues = {
            request.getHeader("X-Forwarded-For"),
            request.getHeader("CF-Connecting-IP"),
            request.getHeader("True-Client-IP")
        };
        
        for (String header : headerValues) {
            if (header != null && !header.isEmpty()) {
                // Si hay múltiples IPs, tomar la primera
                return header.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
}
