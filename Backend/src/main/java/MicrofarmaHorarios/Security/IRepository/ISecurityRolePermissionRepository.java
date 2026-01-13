package MicrofarmaHorarios.Security.IRepository;

import MicrofarmaHorarios.Security.Entity.RolePermission;
import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ISecurityRolePermissionRepository extends ISecurityBaseRepository<RolePermission, String> {
    List<RolePermission> findByRole(Role role);
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);
}