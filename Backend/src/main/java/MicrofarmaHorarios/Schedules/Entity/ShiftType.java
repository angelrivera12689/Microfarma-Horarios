package MicrofarmaHorarios.Schedules.Entity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "shift_type")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "timeRanges")
public class ShiftType extends ASchedulesBaseEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_night_shift", nullable = false)
    private Boolean isNightShift = false;

    /**
     * Indicates if this shift type has multiple time ranges (e.g., PARTIDO shift).
     * When true, the timeRanges list should be used for accurate hour calculations.
     */
    @Column(name = "is_multi_range", nullable = false)
    private Boolean isMultiRange = false;

    /**
     * Ordered list of time ranges for this shift type.
     * For single-range shifts (isMultiRange=false), this list will contain only one element.
     * For multi-range shifts (isMultiRange=true), this list contains all time ranges.
     * 
     * Example for PARTIDO shift:
     * - Range 1: 07:00 - 13:00 (order = 1)
     * - Range 2: 17:00 - 22:00 (order = 2)
     */
    @OneToMany(mappedBy = "shiftType", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rangeOrder ASC")
    private List<ShiftTimeRange> timeRanges = new ArrayList<>();

    /**
     * Adds a time range to this shift type.
     * Automatically sets the shiftType reference and updates isMultiRange flag.
     * 
     * @param startTime Start time of the range
     * @param endTime   End time of the range
     * @param rangeOrder Order of this range (1 for first, 2 for second, etc.)
     */
    public void addTimeRange(LocalTime startTime, LocalTime endTime, Integer rangeOrder) {
        // Initialize list if null
        if (this.timeRanges == null) {
            this.timeRanges = new java.util.ArrayList<>();
        }
        
        ShiftTimeRange range = new ShiftTimeRange(this, startTime, endTime, rangeOrder);
        this.timeRanges.add(range);
        
        // Update isMultiRange flag
        if (this.timeRanges.size() > 1) {
            this.isMultiRange = true;
        }
        
        // Update primary startTime and endTime from first range for backward compatibility
        if (rangeOrder == 1) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.isNightShift = range.getIsNightRange();
        }
    }

    /**
     * Removes all time ranges from this shift type.
     */
    public void clearTimeRanges() {
        this.timeRanges.clear();
        this.isMultiRange = false;
    }

    /**
     * Calculates the total duration in hours for all time ranges.
     * Handles night shifts that cross midnight.
     * 
     * @return Total hours across all time ranges
     */
    public double getTotalDurationHours() {
        return timeRanges.stream()
                .mapToDouble(ShiftTimeRange::getDurationHours)
                .sum();
    }

    /**
     * Gets a formatted string representation of all time ranges.
     * 
     * @return String like "07:00-13:00, 17:00-22:00" for multi-range shifts
     */
    public String getFormattedTimeRanges() {
        if (timeRanges.isEmpty()) {
            return String.format("%02d:%02d - %02d:%02d", 
                startTime.getHour(), startTime.getMinute(),
                endTime.getHour(), endTime.getMinute());
        }
        
        return timeRanges.stream()
                .map(ShiftTimeRange::getFormattedRange)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    /**
     * Checks if this shift is a night shift based on the first time range.
     * For backward compatibility, also checks the legacy isNightShift field.
     * 
     * @return true if any time range is a night shift
     */
    public boolean isNightShiftEffective() {
        if (!timeRanges.isEmpty()) {
            return timeRanges.stream().anyMatch(ShiftTimeRange::getIsNightRange);
        }
        return isNightShift != null && isNightShift;
    }
}