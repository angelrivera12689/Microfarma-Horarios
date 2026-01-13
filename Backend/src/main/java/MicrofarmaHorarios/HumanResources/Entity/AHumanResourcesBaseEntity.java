package MicrofarmaHorarios.HumanResources.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted_at IS NULL")
public abstract class AHumanResourcesBaseEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = true, length = 100)
    private String updatedBy;

    @Column(name = "deleted_by", nullable = true, length = 100)
    private String deletedBy;

    // Getters y Setters de Lombok @Data
}