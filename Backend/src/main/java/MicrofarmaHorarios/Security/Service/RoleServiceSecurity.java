package MicrofarmaHorarios.Security.Service;

import MicrofarmaHorarios.Security.IRepository.ISecurityBaseRepository;
import MicrofarmaHorarios.Security.IRepository.ISecurityRoleRepository;
import MicrofarmaHorarios.Security.IRepository.ISecurityRolePermissionRepository;
import MicrofarmaHorarios.Security.IRepository.ISecurityPermissionRepository;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.Entity.Permission;
import MicrofarmaHorarios.Security.Entity.RolePermission;
import MicrofarmaHorarios.Security.IService.ISecurityRoleService;

@Service
public class RoleServiceSecurity extends ASecurityBaseService<Role> implements ISecurityRoleService {

    @Autowired
    private ISecurityRoleRepository repository;

    @Autowired
    private ISecurityRolePermissionRepository rolePermissionRepository;

    @Autowired
    private ISecurityPermissionRepository permissionRepository;

    @Override
    protected ISecurityBaseRepository<Role, String> getRepository() {
        return repository;
    }

    @Override
    public Optional<Role> findByName(String name) throws Exception {
        return repository.findByName(name);
    }

    @Override
    public List<Permission> getPermissionsForRole(String roleId) throws Exception {
        Role role = findById(roleId).orElseThrow(() -> new Exception("Rol no encontrado"));
        return rolePermissionRepository.findByRole(role).stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
    }

    @Override
    public void assignPermissionToRole(String roleId, String permissionId) throws Exception {
        Role role = findById(roleId).orElseThrow(() -> new Exception("Rol no encontrado"));
        Permission permission = permissionRepository.findById(permissionId).orElseThrow(() -> new Exception("Permiso no encontrado"));

        // Check if already assigned
        boolean exists = rolePermissionRepository.findByRoleAndPermission(role, permission).isPresent();
        if (exists) {
            throw new Exception("El permiso ya está asignado a este rol");
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermission.setStatus(true);
        rolePermission.setCreatedAt(java.time.LocalDateTime.now());
        rolePermission.setCreatedBy("admin");

        rolePermissionRepository.save(rolePermission);
    }

    @Override
    public void removePermissionFromRole(String roleId, String permissionId) throws Exception {
        Role role = findById(roleId).orElseThrow(() -> new Exception("Rol no encontrado"));
        Permission permission = permissionRepository.findById(permissionId).orElseThrow(() -> new Exception("Permiso no encontrado"));

        Optional<RolePermission> rolePermission = rolePermissionRepository.findByRoleAndPermission(role, permission);
        if (rolePermission.isEmpty()) {
            throw new Exception("El permiso no está asignado a este rol");
        }

        rolePermissionRepository.delete(rolePermission.get());
    }

}
