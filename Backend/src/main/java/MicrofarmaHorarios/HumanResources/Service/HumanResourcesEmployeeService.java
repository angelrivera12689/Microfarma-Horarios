package MicrofarmaHorarios.HumanResources.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesBaseRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeRepository;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.Notification.Service.EmailService;
import MicrofarmaHorarios.Security.IService.ISecurityRoleService;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;
import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.Entity.User;

@Service
public class HumanResourcesEmployeeService extends AHumanResourcesBaseService<Employee> implements IHumanResourcesEmployeeService {

    @Autowired
    private IHumanResourcesEmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Lazy
    private ISecurityUserService userService;

    @Autowired
    private ISecurityRoleService roleService;

    @Override
    protected IHumanResourcesBaseRepository<Employee, String> getRepository() {
        return employeeRepository;
    }

    @Override
    public List<Employee> findByStateTrue() throws Exception {
        return super.findByStateTrue().stream()
                .filter(employee -> employee.getUser() == null || "EMPLOYEE".equals(employee.getUser().getRole().getName()))
                .toList();
    }

    @Override
    public Optional<Employee> findByEmail(String email) throws Exception {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public Employee save(Employee entity) throws Exception {
        Employee savedEmployee = super.save(entity);

        // Associate with existing User if email matches, or create new User if none exists
        try {
            Optional<User> existingUser = userService.findByEmail(savedEmployee.getEmail());
            User userToAssociate;
            if (existingUser.isPresent() && existingUser.get().getEmployee() == null) {
                // Use existing user without employee association
                userToAssociate = existingUser.get();
            } else {
                // Create new User with EMPLOYEE role
                userToAssociate = new User();
                userToAssociate.setEmail(savedEmployee.getEmail());
                userToAssociate.setName(savedEmployee.getFirstName() + " " + savedEmployee.getLastName());
                userToAssociate.setActive(true);
                // Set default password (should be changed by user)
                userToAssociate.setPasswordHash("$2a$10$defaulthashedpasswordchangeme");
                
                // Find EMPLOYEE role
                Optional<Role> employeeRole = roleService.findByName("EMPLOYEE");
                if (employeeRole.isPresent()) {
                    userToAssociate.setRole(employeeRole.get());
                } else {
                    throw new Exception("EMPLOYEE role not found");
                }
                
                // Save the new user
                userToAssociate = userService.save(userToAssociate);
            }
            
            // Associate user with employee
            userToAssociate.setEmployee(savedEmployee);
            savedEmployee.setUser(userToAssociate);
            userService.save(userToAssociate); // Update User
            employeeRepository.save(savedEmployee); // Update Employee
        } catch (Exception e) {
            // Log but don't fail
            throw new Exception("Error creating/associating user: " + e.getMessage());
        }

        try {
            emailService.sendWelcomeEmail(savedEmployee);
        } catch (Exception e) {
            // Log error but don't fail the save operation
        }
        return savedEmployee;
    }

}