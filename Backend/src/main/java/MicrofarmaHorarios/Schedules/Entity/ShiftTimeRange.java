package MicrofarmaHorarios.Schedules.Entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity representing a single time range within a ShiftType.
 * Supports multi-range shifts like "PARTIDO" (7:00-13:00 and 17:00-22:00).
 * For backward compatibility, the ShiftType also maintains startTime and endTime fields
 * which are populated from the first time range.
 */
@Entity
@Table(name = "shift_time_range")
@Data
@EqualsAndHashCode(callSuper = false, exclude = "shiftType")
public class ShiftTimeRange extends ASchedulesBaseEntity {

    @ManyToOne
    @JoinColumn(name = "shift_type_id", nullable = false)
    @JsonIgnore
    private ShiftType shiftType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "range_order", nullable = false)
    private Integer rangeOrder = 1;

    @Column(name = "is_night_range", nullable = false)
    private Boolean isNightRange = false;

    /**
     * Default constructor required by JPA.
     */
    public ShiftTimeRange() {
    }

    /**
     * Constructor for creating a time range with start and end times.
     * 
     * @param shiftType  The shift type this range belongs to
     * @param startTime  The start time of the range
     * @param endTime    The end time of the range
     * @param rangeOrder The order of this range (1 for first, 2 for second, etc.)
     */
    public ShiftTimeRange(ShiftType shiftType, LocalTime startTime, LocalTime endTime, Integer rangeOrder) {
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rangeOrder = rangeOrder;
        this.isNightRange = isNightTime(startTime);
    }

    /**
     * Checks if this time range crosses midnight (night shift).
     * 
     * @return true if the end time is before the start time (crosses midnight)
     */
    public boolean isCrossingMidnight() {
        return endTime.isBefore(startTime);
    }

    /**
     * Calculates the duration of this time range in hours.
     * Handles night shifts that cross midnight.
     * 
     * @return The duration in hours
     */
    public double getDurationHours() {
        if (isCrossingMidnight()) {
            // Calculate hours from start to midnight + midnight to end
            int startToMidnight = 24 - startTime.getHour();
            int midnightToEnd = endTime.getHour();
            return startToMidnight + midnightToEnd;
        }
        return (endTime.getHour() - startTime.getHour()) + 
               (endTime.getMinute() - startTime.getMinute()) / 60.0;
    }

    /**
     * Checks if a given time falls within this range.
     * Handles night shifts that cross midnight.
     * 
     * @param time The time to check
     * @return true if the time is within this range
     */
    public boolean containsTime(LocalTime time) {
        if (isCrossingMidnight()) {
            return time.compareTo(startTime) >= 0 || time.compareTo(endTime) <= 0;
        }
        return time.compareTo(startTime) >= 0 && time.compareTo(endTime) <= 0;
    }

    /**
     * Determines if the given time is night time (10 PM - 6 AM).
     * 
     * @param time The time to check
     * @return true if the time is night time
     */
    private boolean isNightTime(LocalTime time) {
        int hour = time.getHour();
        return hour >= 22 || hour < 6;
    }

    /**
     * Returns a formatted string representation of this time range.
     * 
     * @return String in format "HH:mm - HH:mm"
     */
    public String getFormattedRange() {
        return String.format("%02d:%02d - %02d:%02d", 
            startTime.getHour(), startTime.getMinute(),
            endTime.getHour(), endTime.getMinute());
    }
}
