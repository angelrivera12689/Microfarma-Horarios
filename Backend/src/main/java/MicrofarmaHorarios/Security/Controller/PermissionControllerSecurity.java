package MicrofarmaHorarios.Security.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import MicrofarmaHorarios.Security.Entity.Permission;

import MicrofarmaHorarios.Security.IService.ISecurityPermissionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/Permission")
@PreAuthorize("hasRole('ADMIN')")
public class PermissionControllerSecurity extends ASecurityBaseController<Permission, ISecurityPermissionService> {

    public PermissionControllerSecurity(ISecurityPermissionService service) {
        super(service, "Permission");
    }

    @Override
    public ResponseEntity<ApiResponseDto<Permission>> save(@RequestBody Permission entity) {
        try {
            entity.setStatus(true);
            entity.setCreatedAt(java.time.LocalDateTime.now());
            entity.setCreatedBy("admin");
            return ResponseEntity.ok(new ApiResponseDto<Permission>("Permiso creado exitosamente", service.save(entity), true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<Permission>(e.getMessage(), null, false));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Permission>> update(@PathVariable String id, @RequestBody Permission entity) {
        try {
            Permission existing = service.findById(id).orElseThrow(() -> new Exception("Permiso no encontrado"));
            // Update only provided fields
            if (entity.getName() != null) existing.setName(entity.getName());
            if (entity.getDescription() != null) existing.setDescription(entity.getDescription());
            // Auditing will handle updatedAt and updatedBy
            return ResponseEntity.ok(new ApiResponseDto<Permission>("Permiso actualizado exitosamente", service.save(existing), true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<Permission>(e.getMessage(), null, false));
        }
    }

}