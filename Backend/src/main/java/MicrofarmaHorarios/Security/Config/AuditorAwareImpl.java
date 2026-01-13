package MicrofarmaHorarios.Security.Config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import MicrofarmaHorarios.Security.Entity.User;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("system");
        }

        // If it's a UserDetails object, get the username (email)
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return Optional.of(user.getEmail());
        }

        // If it's a String (username), return it
        if (authentication.getPrincipal() instanceof String) {
            return Optional.of((String) authentication.getPrincipal());
        }

        return Optional.of("system");
    }
}