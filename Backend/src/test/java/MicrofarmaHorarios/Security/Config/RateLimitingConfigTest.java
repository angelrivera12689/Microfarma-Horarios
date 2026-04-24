package MicrofarmaHorarios.Security.Config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests para validar Rate Limiting
 * ✅ Asegura que el rate limiting funciona correctamente
 */
class RateLimitingConfigTest {
    
    private RateLimitingConfig rateLimitingConfig;
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    @BeforeEach
    void setUp() {
        rateLimitingConfig = new RateLimitingConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }
    
    @Test
    void testAllowsRequestWithinLimit() throws Exception {
        // Arrange
        MockHttpServletRequest mockRequest = (MockHttpServletRequest) request;
        mockRequest.setRemoteAddr("192.168.1.1");
        mockRequest.setRequestURI("/api/employees");
        MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
        
        // Act - Primera solicitud debe ser permitida
        boolean result = rateLimitingConfig.preHandle(mockRequest, mockResponse, null);
        
        // Assert
        assertTrue(result, "First request should be allowed");
    }
    
    @Test
    void testRateLimitingLoginEndpoint() throws Exception {
        // Arrange
        MockHttpServletRequest mockRequest = (MockHttpServletRequest) request;
        mockRequest.setRemoteAddr("192.168.1.100");
        mockRequest.setRequestURI("/api/auth/login");
        MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
        
        // Act - Login tiene límite de 5 por minuto
        boolean result1 = rateLimitingConfig.preHandle(mockRequest, mockResponse, null);
        boolean result2 = rateLimitingConfig.preHandle(mockRequest, mockResponse, null);
        boolean result3 = rateLimitingConfig.preHandle(mockRequest, mockResponse, null);
        boolean result4 = rateLimitingConfig.preHandle(mockRequest, mockResponse, null);
        boolean result5 = rateLimitingConfig.preHandle(mockRequest, mockResponse, null);
        boolean result6 = rateLimitingConfig.preHandle(mockRequest, mockResponse, null);
        
        // Assert
        assertTrue(result1 && result2 && result3 && result4 && result5, "First 5 should be allowed");
        assertFalse(result6, "6th request to login should be blocked");
        assertEquals(429, mockResponse.getStatus(), "Should return 429 Too Many Requests");
    }
    
    @Test
    void testGetClientIpAddr() {
        // Arrange
        MockHttpServletRequest mockRequest = (MockHttpServletRequest) request;
        mockRequest.addHeader("X-Forwarded-For", "203.0.113.1, 198.51.100.1");
        
        // Act
        String ipAddr = (String) ReflectionTestUtils.invokeMethod(rateLimitingConfig, "getClientIpAddr", mockRequest);
        
        // Assert
        assertEquals("203.0.113.1", ipAddr, "Should extract first IP from X-Forwarded-For");
    }
    
    @Test
    void testDifferentIPsDifferentBuckets() throws Exception {
        // Arrange
        MockHttpServletResponse mockResponse1 = new MockHttpServletResponse();
        MockHttpServletResponse mockResponse2 = new MockHttpServletResponse();
        
        MockHttpServletRequest mockRequest1 = (MockHttpServletRequest) request;
        mockRequest1.setRemoteAddr("192.168.1.1");
        mockRequest1.setRequestURI("/api/employees");
        
        MockHttpServletRequest mockRequest2 = new MockHttpServletRequest();
        mockRequest2.setRemoteAddr("192.168.1.2");
        mockRequest2.setRequestURI("/api/employees");
        
        // Act
        boolean result1 = rateLimitingConfig.preHandle(mockRequest1, mockResponse1, null);
        boolean result2 = rateLimitingConfig.preHandle(mockRequest2, mockResponse2, null);
        
        // Assert
        assertTrue(result1 && result2, "Different IPs should have separate rate limits");
    }
}
