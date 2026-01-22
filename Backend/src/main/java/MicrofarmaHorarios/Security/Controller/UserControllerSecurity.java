package MicrofarmaHorarios.Security.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import MicrofarmaHorarios.Security.Entity.User;
import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;
import MicrofarmaHorarios.Security.IService.ISecurityRoleService;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import MicrofarmaHorarios.Security.DTO.Request.RegisterRequestDto;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;
import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/User")
@PreAuthorize("hasRole('ADMIN')")
public class UserControllerSecurity extends ASecurityBaseController<User, ISecurityUserService> {

    @Autowired
    private ISecurityRoleService roleService;

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserControllerSecurity(ISecurityUserService service) {
        super(service, "User");
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<User>>> findByStateTrue() {
        try {
            List<User> users = service.findByStateTrue();
            System.out.println("Users found: " + users.size());
            for (User u : users) {
                System.out.println("User: " + u.getEmail() + " - Active: " + u.getActive());
            }
            return ResponseEntity.ok(new ApiResponseDto<List<User>>("Datos obtenidos", users, true));
        } catch (Exception e) {
            System.out.println("Error finding users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponseDto<List<User>>(e.getMessage(), null, false));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<User>> save(@RequestBody User entity) {
        try {
            // Encode password if it's set (for new users created by admin)
            if (entity.getPasswordHash() != null && !entity.getPasswordHash().isEmpty()) {
                entity.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));
            }
            entity.setActive(true);
            entity.setStatus(true);
            entity.setCreatedAt(java.time.LocalDateTime.now());
            entity.setCreatedBy("admin");
            return ResponseEntity.ok(new ApiResponseDto<User>("Usuario creado exitosamente", service.save(entity), true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<User>(e.getMessage(), null, false));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<User>> update(@PathVariable String id, @RequestBody User entity) {
        try {
            User existing = service.findById(id).orElseThrow(() -> new Exception("Usuario no encontrado"));
            // Update only provided fields
            if (entity.getName() != null) existing.setName(entity.getName());
            if (entity.getEmail() != null) existing.setEmail(entity.getEmail());
            if (entity.getRole() != null && entity.getRole().getId() != null) {
                Role role = roleService.findById(entity.getRole().getId()).orElse(null);
                if (role != null) {
                    existing.setRole(role);
                }
                // If role not found, don't update the role
            }
            // Do not update password here, as it's not sent for updates
            // Auditing will handle updatedAt and updatedBy
            return ResponseEntity.ok(new ApiResponseDto<User>("Usuario actualizado exitosamente", service.save(existing), true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<User>(e.getMessage(), null, false));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<String>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        try {
            // Check if email already exists
            if (service.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponseDto<String>("El email ya está registrado. Por favor usa un email diferente.", null, false));
            }

            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            Role role = roleService.findByName("USER").orElseThrow(() -> new Exception("Rol por defecto no encontrado. Por favor crea un rol con nombre 'USER'."));
            user.setRole(role);
            user.setActive(true);
            user.setStatus(true);
            user.setCreatedAt(java.time.LocalDateTime.now());
            user.setCreatedBy("system");

            service.save(user);

            return ResponseEntity.ok(new ApiResponseDto<String>("¡Registro exitoso! Bienvenido " + user.getName() + ". Ya puedes iniciar sesión.", null, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Error en el registro: " + e.getMessage(), null, false));
        }
    }

    @PutMapping("/link-employee/{userId}/{employeeId}")
    public ResponseEntity<ApiResponseDto<String>> linkEmployee(@PathVariable String userId, @PathVariable String employeeId) {
        try {
            User user = service.findById(userId).orElseThrow(() -> new Exception("Usuario no encontrado"));
            var employee = employeeService.findById(employeeId).orElseThrow(() -> new Exception("Empleado no encontrado"));

            user.setEmployee(employee);
            service.save(user);

            return ResponseEntity.ok(new ApiResponseDto<String>("Usuario enlazado exitosamente al empleado", null, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Error al enlazar: " + e.getMessage(), null, false));
        }
    }

}

