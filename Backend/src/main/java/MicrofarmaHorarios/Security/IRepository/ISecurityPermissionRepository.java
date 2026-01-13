package MicrofarmaHorarios.Security.IRepository;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Security.Entity.Permission;

@Repository
public interface ISecurityPermissionRepository extends ISecurityBaseRepository<Permission, String> {

}
