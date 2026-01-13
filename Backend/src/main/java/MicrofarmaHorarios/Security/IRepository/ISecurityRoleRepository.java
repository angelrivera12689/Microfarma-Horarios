package MicrofarmaHorarios.Security.IRepository;

import org.springframework.stereotype.Repository;
import java.util.Optional;

import MicrofarmaHorarios.Security.Entity.Role;

@Repository
public interface ISecurityRoleRepository extends ISecurityBaseRepository<Role, String> {
    Optional<Role> findByName(String name);
}
