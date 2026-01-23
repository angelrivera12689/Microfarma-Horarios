package MicrofarmaHorarios.Security.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Security.IRepository.ISecurityBaseRepository;
import MicrofarmaHorarios.Security.IRepository.ISecurityUserRepository;
import MicrofarmaHorarios.Security.Entity.User;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.HumanResources.Entity.Employee;

@Service
public class UserServiceSecurity extends ASecurityBaseService<User> implements ISecurityUserService, UserDetailsService {

    @Override
    protected ISecurityBaseRepository<User, String> getRepository() {
        return repository;
    }

    @Autowired
    private ISecurityUserRepository repository;

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    @Override
    public Optional<User> findByEmail(String email) throws Exception {
        return repository.findByEmail(email);
    }

    @Override
    public User save(User entity) throws Exception {
        User savedUser = super.save(entity);

        // If role is EMPLOYEE, create employee if not exists
        if (savedUser.getRole() != null && "EMPLOYEE".equals(savedUser.getRole().getName())) {
            try {
                Optional<Employee> existingEmployee = employeeService.findByEmail(savedUser.getEmail());
                if (existingEmployee.isEmpty()) {
                    Employee employee = new Employee();
                    employee.setEmail(savedUser.getEmail());
                    employee.setFirstName(savedUser.getName());
                    employee.setLastName("");
                    employee.setHireDate(java.time.LocalDate.now());
                    employee.setUser(savedUser);
                    savedUser.setEmployee(employee);
                    employeeService.save(employee);
                } else if (existingEmployee.get().getUser() == null) {
                    Employee employee = existingEmployee.get();
                    employee.setUser(savedUser);
                    savedUser.setEmployee(employee);
                    employeeService.save(employee);
                }
            } catch (Exception e) {
                // Log but don't fail
            }
        } else {
            // Associate with existing Employee if email matches
            try {
                Optional<Employee> existingEmployee = employeeService.findByEmail(savedUser.getEmail());
                if (existingEmployee.isPresent() && existingEmployee.get().getUser() == null) {
                    Employee employee = existingEmployee.get();
                    employee.setUser(savedUser);
                    savedUser.setEmployee(employee);
                    employeeService.save(employee); // Update Employee
                    repository.save(savedUser); // Update User
                }
            } catch (Exception e) {
                // Log but don't fail
            }
        }

        return savedUser;
    }

    @Override
    public void updatePassword(User user, String encodedNewPassword) throws Exception {
        user.setPasswordHash(encodedNewPassword);
        repository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = null;
        try {
            user = repository.findByEmailWithRoleAndPermissions(username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return user.get();
    }
}
