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
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesPositionService;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesContractTypeService;
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
    private IHumanResourcesPositionService positionService;

    @Autowired
    private IHumanResourcesContractTypeService contractTypeService;

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
            System.out.println("Received entity in save: " + entity);
            if (entity != null) {
                System.out.println("Entity name: " + entity.getName() + ", email: " + entity.getEmail() + ", passwordHash: " + (entity.getPasswordHash() != null ? "present" : "null"));
            }
            if (entity == null) {
                System.out.println("Entity is null, returning bad request");
                return ResponseEntity.badRequest().body(new ApiResponseDto<User>("La entidad de usuario no puede ser nula", null, false));
            }
            // Validate required fields
            if (entity.getName() == null || entity.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponseDto<User>("El nombre es obligatorio", null, false));
            }
            if (entity.getEmail() == null || entity.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponseDto<User>("El email es obligatorio", null, false));
            }
            if (entity.getPasswordHash() == null || entity.getPasswordHash().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponseDto<User>("La contraseña es obligatoria", null, false));
            }
            // Encode password if it's set (for new users created by admin)
            entity.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));
            entity.setActive(true);
            entity.setStatus(true);
            entity.setCreatedAt(java.time.LocalDateTime.now());
            entity.setCreatedBy("admin");

            User savedUser = service.save(entity);

            // Create employee for the new user
            MicrofarmaHorarios.HumanResources.Entity.Employee employee = new MicrofarmaHorarios.HumanResources.Entity.Employee();
            String[] nameParts = savedUser.getName().split(" ");
            employee.setFirstName(nameParts[0]);
            employee.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            employee.setEmail(savedUser.getEmail());
            employee.setHireDate(java.time.LocalDate.now());

            // Get default position and contract type
            try {
                var positions = positionService.all();
                if (!positions.isEmpty()) {
                    employee.setPosition(positions.get(0));
                }
                var contracts = contractTypeService.all();
                if (!contracts.isEmpty()) {
                    employee.setContractType(contracts.get(0));
                }
            } catch (Exception e) {
                // Ignore if services fail
            }

            employee = employeeService.save(employee);
            savedUser.setEmployee(employee);
            savedUser = service.save(savedUser);

            return ResponseEntity.ok(new ApiResponseDto<User>("Usuario creado exitosamente", savedUser, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<User>("Error al guardar el usuario: " + e.getMessage(), null, false));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<User>> update(@PathVariable String id, @RequestBody User entity) {
        try {
            System.out.println("Updating user " + id + " with role id: " + (entity.getRole() != null ? entity.getRole().getId() : "null"));
            User existing = service.findById(id).orElseThrow(() -> new Exception("Usuario no encontrado"));
            // Update only provided fields
            if (entity.getName() != null) existing.setName(entity.getName());
            if (entity.getEmail() != null) existing.setEmail(entity.getEmail());
            if (entity.getRole() != null && entity.getRole().getId() != null) {
                Role role = roleService.findById(entity.getRole().getId()).orElse(null);
                if (role != null) {
                    existing.setRole(role);
                    // If role is EMPLOYEE and no employee exists, create one
                    if ("EMPLOYEE".equals(role.getName()) && existing.getEmployee() == null) {
                        MicrofarmaHorarios.HumanResources.Entity.Employee employee = new MicrofarmaHorarios.HumanResources.Entity.Employee();
                        String[] nameParts = existing.getName().split(" ");
                        employee.setFirstName(nameParts[0]);
                        employee.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                        employee.setEmail(existing.getEmail());
                        employee.setHireDate(java.time.LocalDate.now());

                        // Get default position and contract type
                        try {
                            var positions = positionService.all();
                            if (!positions.isEmpty()) {
                                employee.setPosition(positions.get(0));
                            }
                            var contracts = contractTypeService.all();
                            if (!contracts.isEmpty()) {
                                employee.setContractType(contracts.get(0));
                            }
                        } catch (Exception e) {
                            // Ignore if services fail
                        }

                        employee = employeeService.save(employee);
                        existing.setEmployee(employee);
                    }
                    // Update employee based on role
                    if (existing.getEmployee() != null) {
                        if ("EMPLOYEE".equals(role.getName())) {
                            existing.getEmployee().setStatus(true);
                            employeeService.save(existing.getEmployee());
                        } else {
                            // Keep the employee linked and active to allow shift assignments even with non-EMPLOYEE roles
                            // No changes needed to the employee
                        }
                    } else {
                        // Create employee for any user that doesn't have one
                        MicrofarmaHorarios.HumanResources.Entity.Employee employee = new MicrofarmaHorarios.HumanResources.Entity.Employee();
                        String[] nameParts = existing.getName().split(" ");
                        employee.setFirstName(nameParts[0]);
                        employee.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                        employee.setEmail(existing.getEmail());
                        employee.setHireDate(java.time.LocalDate.now());

                        // Get default position and contract type
                        try {
                            var positions = positionService.all();
                            if (!positions.isEmpty()) {
                                employee.setPosition(positions.get(0));
                            }
                            var contracts = contractTypeService.all();
                            if (!contracts.isEmpty()) {
                                employee.setContractType(contracts.get(0));
                            }
                        } catch (Exception e) {
                            // Ignore if services fail
                        }

                        employee = employeeService.save(employee);
                        existing.setEmployee(employee);
                    }
                }
                // If role not found, don't update the role
            }
            // Do not update password here, as it's not sent for updates
            // Auditing will handle updatedAt and updatedBy
            return ResponseEntity.ok(new ApiResponseDto<User>("Usuario actualizado exitosamente", service.save(existing), true));
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
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

            User savedUser = service.save(user);

            // Create employee for the new user
            MicrofarmaHorarios.HumanResources.Entity.Employee employee = new MicrofarmaHorarios.HumanResources.Entity.Employee();
            String[] nameParts = savedUser.getName().split(" ");
            employee.setFirstName(nameParts[0]);
            employee.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            employee.setEmail(savedUser.getEmail());
            employee.setHireDate(java.time.LocalDate.now());

            // Get default position and contract type
            try {
                var positions = positionService.all();
                if (!positions.isEmpty()) {
                    employee.setPosition(positions.get(0));
                }
                var contracts = contractTypeService.all();
                if (!contracts.isEmpty()) {
                    employee.setContractType(contracts.get(0));
                }
            } catch (Exception e) {
                // Ignore if services fail
            }

            employee = employeeService.save(employee);
            savedUser.setEmployee(employee);
            service.save(savedUser);

            return ResponseEntity.ok(new ApiResponseDto<String>("¡Registro exitoso! Bienvenido " + savedUser.getName() + ". Ya puedes iniciar sesión.", null, true));
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

    @PutMapping("/promote-to-employee/{userId}")
    public ResponseEntity<ApiResponseDto<String>> promoteToEmployee(@PathVariable String userId) {
        try {
            User user = service.findById(userId).orElseThrow(() -> new Exception("Usuario no encontrado"));
            Role employeeRole = roleService.findByName("EMPLOYEE").orElseThrow(() -> new Exception("Rol EMPLOYEE no encontrado"));

            // Create employee if not exists
            if (user.getEmployee() == null) {
                MicrofarmaHorarios.HumanResources.Entity.Employee employee = new MicrofarmaHorarios.HumanResources.Entity.Employee();
                String[] nameParts = user.getName().split(" ");
                employee.setFirstName(nameParts[0]);
                employee.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                employee.setEmail(user.getEmail());
                employee.setHireDate(java.time.LocalDate.now());

                // Get default position and contract type
                try {
                    var positions = positionService.all();
                    if (!positions.isEmpty()) {
                        employee.setPosition(positions.get(0));
                    }
                    var contracts = contractTypeService.all();
                    if (!contracts.isEmpty()) {
                        employee.setContractType(contracts.get(0));
                    }
                } catch (Exception e) {
                    // Ignore if services fail
                }

                employeeService.save(employee);
                user.setEmployee(employee);
            }

            user.setRole(employeeRole);
            service.save(user);

            return ResponseEntity.ok(new ApiResponseDto<String>("Usuario promovido exitosamente a EMPLOYEE", null, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<String>("Error al promover: " + e.getMessage(), null, false));
        }
    }

}

