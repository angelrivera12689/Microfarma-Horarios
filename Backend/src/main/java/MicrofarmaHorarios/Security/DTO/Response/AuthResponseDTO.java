package MicrofarmaHorarios.Security.DTO.Response;

import java.io.Serializable;

/**
 * DTO para respuestas de autenticación
 * ✅ No expone entidades completas
 */
public class AuthResponseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String name;
    private String role;
    private Long expiresIn;

    public AuthResponseDTO(String token, String refreshToken, Long id, String email, String name, String role, Long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public AuthResponseDTO() {}

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
