package MicrofarmaHorarios.Security.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import MicrofarmaHorarios.Security.DTO.Request.*;
import MicrofarmaHorarios.Security.DTO.Response.*;

import java.util.Map;

import MicrofarmaHorarios.Security.Entity.User;

import MicrofarmaHorarios.Security.Entity.Role;

import MicrofarmaHorarios.Security.Entity.RefreshToken;

import MicrofarmaHorarios.Security.IService.ISecurityUserService;

import MicrofarmaHorarios.Security.IService.ISecurityRoleService;

import MicrofarmaHorarios.Security.IService.ISecurityRefreshTokenService;

import MicrofarmaHorarios.Security.IService.ISecurityPasswordResetTokenService;

import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesPositionService;
import MicrofarmaHorarios.HumanResources.Entity.Position;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesContractTypeService;
import MicrofarmaHorarios.HumanResources.Entity.ContractType;

import MicrofarmaHorarios.Notification.IService.IEmailService;

import MicrofarmaHorarios.Security.Utils.JwtUtils;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Map;

@RestController

@RequestMapping("/api/auth")

@CrossOrigin(origins = "*")

public class AuthControllerSecurity {

    @Autowired

    private ISecurityUserService userService;

    @Autowired

    private ISecurityRoleService roleService;

    @Autowired

    private ISecurityRefreshTokenService refreshTokenService;

    @Autowired

    private IHumanResourcesEmployeeService employeeService;

    @Autowired

    private IHumanResourcesPositionService positionService;

    @Autowired

    private IHumanResourcesContractTypeService contractTypeService;

    @Autowired

    private JwtUtils jwtUtils;

    @Autowired

    private PasswordEncoder passwordEncoder;

    @Autowired

    private ISecurityPasswordResetTokenService passwordResetTokenService;

    @Autowired

    private IEmailService emailService;

    @PostMapping("/login")

    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {

        try {

            User user = userService.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new Exception("Usuario no encontrado"));

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {

                return ResponseEntity.badRequest().body(new ApiResponseDto<LoginResponseDto>("Contraseña incorrecta", null, false));

            }

            String token = jwtUtils.generateToken(user.getEmail());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getEmail());

            LoginResponseDto response = new LoginResponseDto();

            response.setToken(token);

            response.setRefreshToken(refreshToken.getToken());

            response.setId(user.getId());

            response.setEmail(user.getEmail());

            response.setRole(user.getRole().getName());

            return ResponseEntity.ok(new ApiResponseDto<LoginResponseDto>("¡Bienvenido " + user.getName() + "! Has iniciado sesión correctamente.", response, true));

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(new ApiResponseDto<LoginResponseDto>(e.getMessage(), null, false));

        }

    }

    @PostMapping("/register")

    public ResponseEntity<ApiResponseDto<String>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {

        try {

            // Check if email already exists
            if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponseDto<String>("El email ya está registrado. Por favor usa un email diferente.", null, false));
            }

            User user = new User();

            user.setName(registerRequest.getName());

            user.setEmail(registerRequest.getEmail());

            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

            Role role = roleService.findByName("USER").orElseThrow(() -> new Exception("Rol por defecto no encontrado. Por favor crea un rol con nombre 'USER'."));

            user.setRole(role);

            user.setActive(true);

            userService.save(user);

            return ResponseEntity.ok(new ApiResponseDto<String>("¡Registro exitoso! Bienvenido " + user.getName() + ". Ya puedes iniciar sesión.", null, true));

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Error en el registro: " + e.getMessage(), null, false));

        }

    }

    @PostMapping("/refresh")

    public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshTokenRequestDto refreshRequest) throws Exception {

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshRequest.getRefreshToken());

        if (refreshToken.getRevoked() || refreshToken.getExpirationDate().before(new Date())) {

            throw new Exception("Invalid refresh token");

        }

        User user = userService.findById(refreshToken.getUserId()).orElseThrow(() -> new Exception("User not found"));

        String newToken = jwtUtils.generateToken(user.getEmail());

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getEmail());

        LoginResponseDto response = new LoginResponseDto();

        response.setToken(newToken);

        response.setRefreshToken(newRefreshToken.getToken());

        response.setId(user.getId());

        response.setEmail(user.getEmail());

        response.setRole(user.getRole().getName());

        return ResponseEntity.ok(response);

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto forgotRequest) {
        try {
            User user = userService.findByEmail(forgotRequest.getEmail()).orElseThrow(() -> new Exception("Usuario no encontrado"));

            MicrofarmaHorarios.Security.Entity.PasswordResetToken token = passwordResetTokenService.createToken(user);

            emailService.sendPasswordResetEmail(user, token.getToken());

            return ResponseEntity.ok(new ApiResponseDto<String>("Se ha enviado un email con las instrucciones para recuperar tu contraseña.", null, true));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Error: " + e.getMessage(), null, false));
        }
    }

    public static class VerifyTokenRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    @RequestMapping(value = "/verify-token", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ApiResponseDto<String>> verifyToken(@RequestParam(required = false) String token, @RequestBody(required = false) Map<String, Object> body) {
        try {
            System.out.println("Verify token request received, query token: " + token + ", body: " + body);
            if (passwordResetTokenService == null) {
                System.err.println("passwordResetTokenService is null");
                return ResponseEntity.status(500).body(new ApiResponseDto<String>("Servicio no disponible", null, false));
            }

            String tokenValue = token;
            if (tokenValue == null && body != null) {
                Object tokenObj = body.get("token");
                if (tokenObj == null) {
                    tokenObj = body.get("code"); // Try "code" if "token" not found
                }
                if (tokenObj instanceof String) {
                    tokenValue = (String) tokenObj;
                } else if (tokenObj instanceof Map) {
                    // Handle nested map
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nested = (Map<String, Object>) tokenObj;
                    Object nestedToken = nested.get("token");
                    if (nestedToken == null) {
                        nestedToken = nested.get("code");
                    }
                    if (nestedToken instanceof String) {
                        tokenValue = (String) nestedToken;
                    }
                }
            }

            System.out.println("Token extracted: " + tokenValue);
            if (tokenValue == null || tokenValue.trim().isEmpty()) {
                System.out.println("Token is null or empty");
                return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Token es requerido.", null, false));
            }

            boolean isValid = passwordResetTokenService.isTokenValid(tokenValue);
            System.out.println("Token valid: " + isValid);
            if (isValid) {
                return ResponseEntity.ok(new ApiResponseDto<String>("Token válido.", null, true));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Token inválido o expirado.", null, false));
            }
        } catch (Exception e) {
            System.err.println("Error verifying token: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponseDto<String>("Error interno del servidor: " + e.getMessage(), null, false));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetRequest) {
        try {
            if (!passwordResetTokenService.isTokenValid(resetRequest.getToken())) {
                return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Token inválido o expirado.", null, false));
            }

            MicrofarmaHorarios.Security.Entity.PasswordResetToken token = passwordResetTokenService.findByToken(resetRequest.getToken()).orElseThrow(() -> new Exception("Token no encontrado"));

            User user = token.getUser();
            userService.updatePassword(user, passwordEncoder.encode(resetRequest.getNewPassword()));

            passwordResetTokenService.invalidateToken(resetRequest.getToken());

            return ResponseEntity.ok(new ApiResponseDto<String>("Contraseña actualizada exitosamente.", null, true));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Error: " + e.getMessage(), null, false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<String>> logout() {
        // In a real implementation, you might invalidate the token server-side
        // For now, client-side token removal is sufficient
        return ResponseEntity.ok(new ApiResponseDto<String>("Sesión cerrada exitosamente", null, true));
    }

}
