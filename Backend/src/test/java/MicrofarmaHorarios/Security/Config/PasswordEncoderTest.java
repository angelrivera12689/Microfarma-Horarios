package MicrofarmaHorarios.Security.Config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests para validar que BCrypt funciona correctamente
 * ✅ Asegura que las contraseñas se cifren adecuadamente
 */
class PasswordEncoderTest {
    
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }
    
    @Test
    void testPasswordEncoding() {
        // Arrange
        String rawPassword = "MySecurePassword123!";
        
        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Assert
        assertNotEquals(rawPassword, encodedPassword, "Password should be encoded");
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertTrue(encodedPassword.length() > 20, "BCrypt encoded password should be long");
    }
    
    @Test
    void testPasswordMatching() {
        // Arrange
        String rawPassword = "MySecurePassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Act
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        
        // Assert
        assertTrue(matches, "Raw password should match encoded password");
    }
    
    @Test
    void testPasswordMismatch() {
        // Arrange
        String password1 = "MySecurePassword123!";
        String password2 = "DifferentPassword456!";
        String encodedPassword = passwordEncoder.encode(password1);
        
        // Act
        boolean matches = passwordEncoder.matches(password2, encodedPassword);
        
        // Assert
        assertFalse(matches, "Different password should not match");
    }
    
    @Test
    void testPasswordEncodingConsistency() {
        // Arrange
        String rawPassword = "MySecurePassword123!";
        
        // Act - BCrypt genera diferentes hashes cada vez
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);
        
        // Assert
        assertNotEquals(encoded1, encoded2, "BCrypt should generate different hashes");
        assertTrue(passwordEncoder.matches(rawPassword, encoded1), "Both should match");
        assertTrue(passwordEncoder.matches(rawPassword, encoded2), "Both should match");
    }
}
