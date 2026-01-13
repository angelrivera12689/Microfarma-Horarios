package MicrofarmaHorarios.Schedules.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "calendar")
@Data
public class Calendar extends ASchedulesBaseEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday = false;

    @Column(name = "is_workday", nullable = false)
    private Boolean isWorkday = true;
}