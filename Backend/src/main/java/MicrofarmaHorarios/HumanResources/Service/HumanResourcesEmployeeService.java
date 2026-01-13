package MicrofarmaHorarios.HumanResources.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesBaseRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeRepository;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.Notification.Service.EmailService;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;
import MicrofarmaHorarios.Security.IService.ISecurityRoleService;
import MicrofarmaHorarios.Security.Entity.User;
import MicrofarmaHorarios.Security.Entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;

@Service
public class HumanResourcesEmployeeService extends AHumanResourcesBaseService<Employee> implements IHumanResourcesEmployeeService {

    @Autowired
    private IHumanResourcesEmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ISecurityUserService userService;

    @Autowired
    private ISecurityRoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected IHumanResourcesBaseRepository<Employee, String> getRepository() {
        return employeeRepository;
    }

    @Override
    public Optional<Employee> findByEmail(String email) throws Exception {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public Employee save(Employee entity) throws Exception {
        boolean isNew = (entity.getId() == null);
        Employee savedEmployee = super.save(entity);

        if (isNew) {
            try {
                // Check if user already exists
                if (userService.findByEmail(savedEmployee.getEmail()).isEmpty()) {
                    // Generate temporary password
                    String tempPassword = UUID.randomUUID().toString().substring(0, 8);

                    // Create user
                    User newUser = new User();
                    newUser.setName(savedEmployee.getFirstName() + " " + savedEmployee.getLastName());
                    newUser.setEmail(savedEmployee.getEmail());
                    newUser.setPasswordHash(passwordEncoder.encode(tempPassword));
                    Role employeeRole = roleService.findByName("EMPLOYEE").orElseThrow(() -> new Exception("Rol EMPLOYEE no encontrado"));
                    newUser.setRole(employeeRole);
                    newUser.setActive(true);

                    User savedUser = userService.save(newUser);

                    // Link user to employee
                    savedEmployee.setUser(savedUser);
                    savedEmployee = super.save(savedEmployee); // Save again with the relationship

                    // Send welcome email with temporary password
                    emailService.sendWelcomeEmail(savedEmployee, tempPassword);
                } else {
                    // User already exists, just send welcome email
                    emailService.sendWelcomeEmail(savedEmployee);
                }
            } catch (Exception e) {
                // Log error but don't fail the save operation
                System.err.println("Error creating user or sending welcome email: " + e.getMessage());
            }
        }
        return savedEmployee;
    }

}