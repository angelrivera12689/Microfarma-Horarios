package MicrofarmaHorarios.Schedules.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.Organization.Entity.Location;

@Entity
@Table(name = "shift")
@Data
public class Shift extends ASchedulesBaseEntity {
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "shift_type_id", nullable = false)
    private ShiftType shiftType;

    @Column(name = "notes", length = 255)
    private String notes;
}