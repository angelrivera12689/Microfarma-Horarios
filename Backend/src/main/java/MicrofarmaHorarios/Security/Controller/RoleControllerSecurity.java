package MicrofarmaHorarios.Security.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.Entity.Permission;

import MicrofarmaHorarios.Security.IService.ISecurityRoleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;
import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/Role")
@PreAuthorize("hasRole('ADMIN')")
public class RoleControllerSecurity extends ASecurityBaseController<Role, ISecurityRoleService> {

    public RoleControllerSecurity(ISecurityRoleService service) {
        super(service, "Role");
    }

    @Override
    public ResponseEntity<ApiResponseDto<Role>> save(@RequestBody Role entity) {
        try {
            entity.setStatus(true);
            entity.setCreatedAt(java.time.LocalDateTime.now());
            entity.setCreatedBy("admin");
            return ResponseEntity.ok(new ApiResponseDto<Role>("Rol creado exitosamente", service.save(entity), true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<Role>(e.getMessage(), null, false));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Role>> update(@PathVariable String id, @RequestBody Role entity) {
        try {
            Role existing = service.findById(id).orElseThrow(() -> new Exception("Rol no encontrado"));
            // Update only provided fields
            if (entity.getName() != null) existing.setName(entity.getName());
            // Auditing will handle updatedAt and updatedBy
            return ResponseEntity.ok(new ApiResponseDto<Role>("Rol actualizado exitosamente", service.save(existing), true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<Role>(e.getMessage(), null, false));
        }
    }

    @GetMapping("{id}/permissions")
    public ResponseEntity<ApiResponseDto<List<Permission>>> getPermissionsForRole(@PathVariable String id) {
        try {
            List<Permission> permissions = service.getPermissionsForRole(id);
            return ResponseEntity.ok(new ApiResponseDto<List<Permission>>("Permisos obtenidos", permissions, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<List<Permission>>(e.getMessage(), null, false));
        }
    }

    @PostMapping("{id}/permissions/{permissionId}")
    public ResponseEntity<ApiResponseDto<String>> assignPermissionToRole(@PathVariable String id, @PathVariable String permissionId) {
        try {
            service.assignPermissionToRole(id, permissionId);
            return ResponseEntity.ok(new ApiResponseDto<String>("Permiso asignado al rol exitosamente", null, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<String>(e.getMessage(), null, false));
        }
    }

    @DeleteMapping("{id}/permissions/{permissionId}")
    public ResponseEntity<ApiResponseDto<String>> removePermissionFromRole(@PathVariable String id, @PathVariable String permissionId) {
        try {
            service.removePermissionFromRole(id, permissionId);
            return ResponseEntity.ok(new ApiResponseDto<String>("Permiso removido del rol exitosamente", null, true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<String>(e.getMessage(), null, false));
        }
    }

}
