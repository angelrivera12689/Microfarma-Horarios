package MicrofarmaHorarios.Security.IService;

import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.Entity.Permission;
import java.util.Optional;
import java.util.List;

public interface ISecurityRoleService extends ISecurityBaseService<Role> {
    Optional<Role> findByName(String name) throws Exception;
    List<Permission> getPermissionsForRole(String roleId) throws Exception;
    void assignPermissionToRole(String roleId, String permissionId) throws Exception;
    void removePermissionFromRole(String roleId, String permissionId) throws Exception;
}