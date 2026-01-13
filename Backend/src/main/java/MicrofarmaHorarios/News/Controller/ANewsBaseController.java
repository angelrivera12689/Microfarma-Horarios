package MicrofarmaHorarios.News.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

import MicrofarmaHorarios.News.Entity.ANewsBaseEntity;
import MicrofarmaHorarios.News.IService.INewsBaseService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

/**
 * Abstract base controller providing common CRUD endpoints for entities.
 *
 * @param <T> The type of entity extending ANewsBaseEntity.
 * @param <S> The type of service implementing INewsBaseService for the entity.
 */
public abstract class ANewsBaseController<T extends ANewsBaseEntity, S extends INewsBaseService<T>> {

    protected S service;
    protected String entityName;

    /**
     * Constructor for ANewsBaseController.
     *
     * @param service    The service for the entity.
     * @param entityName The name of the entity.
     */
    protected ANewsBaseController(S service, String entityName) {
        this.service = service;
        this.entityName = entityName;
    }

    /**
     * Retrieves all entities with state set to true.
     *
     * @return A ResponseEntity containing ApiResponseDto with list of entities if
     *         successful,
     *         or error message if unsuccessful.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<T>>> findByStateTrue() {
        try {
            return ResponseEntity.ok(new ApiResponseDto<List<T>>("Datos obtenidos", service.findByStateTrue(), true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponseDto<List<T>>(e.getMessage(), null, false));
        }
    }

    /**
     * Retrieves an entity by its ID.
     *
     * @param id The ID of the entity to retrieve.
     * @return A ResponseEntity containing ApiResponseDto with the entity if found,
     *         or error message if not found.
     */
    @GetMapping("{id}")
    public ResponseEntity<ApiResponseDto<Optional<T>>> show(@PathVariable String id) {
        try {
            Optional<T> entity = service.findById(id);
            return ResponseEntity.ok(new ApiResponseDto<Optional<T>>("Registro encontrado", entity, true));
        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<Optional<T>>(e.getMessage(), null, false));
        }
    }

    /**
     * Saves an entity.
     *
     * @param entity The entity to save.
     * @return A ResponseEntity containing ApiResponseDto with the saved entity if
     *         successful,
     *         or error message if unsuccessful.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<T>> save(@Valid @RequestBody T entity) {
        try {
            return ResponseEntity.ok(new ApiResponseDto<T>("Datos guardados", service.save(entity), true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponseDto<T>(e.getMessage(), null, false));
        }
    }

    /**
     * Updates an existing entity by its ID.
     *
     * @param id     The ID of the entity to update.
     * @param entity The updated entity.
     * @return A ResponseEntity containing ApiResponseDto with success message if
     *         successful,
     *         or error message if unsuccessful.
     */
    @PutMapping("{id}")
    public ResponseEntity<ApiResponseDto<T>> update(@PathVariable String id, @Valid @RequestBody T entity) {
        try {
            service.update(id, entity);
            return ResponseEntity.ok(new ApiResponseDto<T>("Datos actualizados", null, true));
        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("inhabilitado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(new ApiResponseDto<T>(e.getMessage(), null, false));
        }
    }

    /**
     * Deletes an entity by its ID.
     *
     * @param id The ID of the entity to delete.
     * @return A ResponseEntity containing ApiResponseDto with success message if
     *         successful,
     *         or error message if unsuccessful.
     */
    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponseDto<T>> delete(@PathVariable String id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(new ApiResponseDto<T>("Registro eliminado", null, true));
        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(new ApiResponseDto<T>(e.getMessage(), null, false));
        }
    }
}