package MicrofarmaHorarios.HumanResources.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "contract_type")
@Data
public class ContractType extends AHumanResourcesBaseEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "duration_months")
    private Integer durationMonths;
}