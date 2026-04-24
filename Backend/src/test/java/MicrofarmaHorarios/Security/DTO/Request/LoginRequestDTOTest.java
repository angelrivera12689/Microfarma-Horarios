package MicrofarmaHorarios.Security.DTO.Request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

/**
 * Tests para validar DTOs
 * ✅ Asegura que la validación funciona correctamente
 */
class LoginRequestDTOTest {
    
    private Validator validator;
    
    public LoginRequestDTOTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    @Test
    void testValidLoginRequest() {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("SecurePassword123");
        
        // Act
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequest);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid login request should have no violations");
    }
    
    @Test
    void testMissingEmail() {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("");
        loginRequest.setPassword("SecurePassword123");
        
        // Act
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequest);
        
        // Assert
        assertFalse(violations.isEmpty(), "Missing email should cause violation");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("obligatorio")), 
                   "Should mention email in violation");
    }
    
    @Test
    void testInvalidEmail() {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("notanemail");
        loginRequest.setPassword("SecurePassword123");
        
        // Act
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequest);
        
        // Assert
        assertFalse(violations.isEmpty(), "Invalid email should cause violation");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("inválido")), 
                   "Should mention valid email in violation");
    }
    
    @Test
    void testShortPassword() {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("short");
        
        // Act
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequest);
        
        // Assert
        assertFalse(violations.isEmpty(), "Short password should cause violation");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("contraseña")), 
                   "Should mention password in violation");
    }
}
