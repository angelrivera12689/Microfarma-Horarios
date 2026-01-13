package MicrofarmaHorarios.Security.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import MicrofarmaHorarios.Security.Entity.Permission;
import MicrofarmaHorarios.Security.IService.ISecurityPermissionService;
import MicrofarmaHorarios.Security.IRepository.ISecurityBaseRepository;
import MicrofarmaHorarios.Security.IRepository.ISecurityPermissionRepository;

@Service
public class SecurityPermissionService extends ASecurityBaseService<Permission> implements ISecurityPermissionService {

    @Autowired
    private ISecurityPermissionRepository repository;

    @Override
    protected ISecurityBaseRepository<Permission, String> getRepository() {
        return repository;
    }
}