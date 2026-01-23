package MicrofarmaHorarios.HumanResources.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesBaseRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeRepository;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.Notification.Service.EmailService;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;
import MicrofarmaHorarios.Security.Entity.User;

@Service
public class HumanResourcesEmployeeService extends AHumanResourcesBaseService<Employee> implements IHumanResourcesEmployeeService {

    @Autowired
    private IHumanResourcesEmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ISecurityUserService userService;

    @Override
    protected IHumanResourcesBaseRepository<Employee, String> getRepository() {
        return employeeRepository;
    }

    @Override
    public List<Employee> findByStateTrue() throws Exception {
        return super.findByStateTrue().stream()
                .filter(employee -> employee.getUser() != null && "EMPLOYEE".equals(employee.getUser().getRole().getName()))
                .toList();
    }

    @Override
    public Optional<Employee> findByEmail(String email) throws Exception {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public Employee save(Employee entity) throws Exception {
        Employee savedEmployee = super.save(entity);

        // Associate with existing User if email matches
        try {
            Optional<User> existingUser = userService.findByEmail(savedEmployee.getEmail());
            if (existingUser.isPresent() && existingUser.get().getEmployee() == null) {
                User user = existingUser.get();
                user.setEmployee(savedEmployee);
                savedEmployee.setUser(user);
                userService.save(user); // Update User
                employeeRepository.save(savedEmployee); // Update Employee
            }
        } catch (Exception e) {
            // Log but don't fail
        }

        try {
            emailService.sendWelcomeEmail(savedEmployee);
        } catch (Exception e) {
            // Log error but don't fail the save operation
        }
        return savedEmployee;
    }

}