package MicrofarmaHorarios.Security.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.Entity.User;
import MicrofarmaHorarios.Security.IService.ISecurityRoleService;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ISecurityRoleService roleService;

    @Autowired
    private ISecurityUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default roles if they don't exist
        createRoleIfNotExists("USER");
        createRoleIfNotExists("EMPLOYEE");
        createRoleIfNotExists("ADMIN");

        // Create default admin user if it doesn't exist
        createAdminUserIfNotExists();
    }

    private void createRoleIfNotExists(String roleName) throws Exception {
        if (roleService.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleService.save(role);
            System.out.println("Role " + roleName + " created successfully");
        }
    }

    private void createAdminUserIfNotExists() throws Exception {
        if (userService.findByEmail("admin@microfarma.com").isEmpty()) {
            User adminUser = new User();
            adminUser.setName("Administrador");
            adminUser.setEmail("admin@microfarma.com");
            adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
            adminUser.setActive(true);

            Role adminRole = roleService.findByName("ADMIN").orElseThrow(() -> new Exception("Admin role not found"));
            adminUser.setRole(adminRole);

            userService.save(adminUser);
            System.out.println("Default admin user created: admin@microfarma.com / admin123");
        }
    }
}
