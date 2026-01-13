package MicrofarmaHorarios.HumanResources.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "employee_location")
@Data
public class EmployeeLocation extends AHumanResourcesBaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private MicrofarmaHorarios.Organization.Entity.Location location;
}