package MicrofarmaHorarios.Schedules.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Security.Entity.User;

@Entity
@Table(name = "shift_change_request")
@Data
public class ShiftChangeRequest extends ASchedulesBaseEntity {

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "original_shift_id", nullable = true)
    private Shift originalShift;

    @Column(name = "requested_date")
    private LocalDate requestedDate;

    @ManyToOne
    @JoinColumn(name = "requested_shift_type_id")
    private ShiftType requestedShiftType;

    @ManyToOne
    @JoinColumn(name = "requested_location_id")
    private Location requestedLocation;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private RequestStatus requestStatus = RequestStatus.PENDING;

    @Column(name = "admin_decision", length = 500)
    private String adminDecision;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
}