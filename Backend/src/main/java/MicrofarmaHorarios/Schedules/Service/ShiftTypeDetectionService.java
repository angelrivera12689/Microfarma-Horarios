package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import MicrofarmaHorarios.Schedules.Entity.ShiftTimeRange;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftTypeRepository;

/**
 * Service for detecting and managing shift types during PDF import.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftTypeDetectionService {

    private final ISchedulesShiftTypeRepository shiftTypeRepository;
    
    // Morning shift: 08:00 - 16:00
    private static final String MAÑANA_NAME = "Mañana";
    private static final LocalTime MAÑANA_START = LocalTime.of(8, 0);
    private static final LocalTime MAÑANA_END = LocalTime.of(16, 0);
    
    // Afternoon shift: 14:00 - 22:00
    private static final String TARDE_NAME = "Tarde";
    private static final LocalTime TARDE_START = LocalTime.of(14, 0);
    private static final LocalTime TARDE_END = LocalTime.of(22, 0);
    
    // Night shift: 22:00 - 06:00
    private static final String NOCHE_NAME = "Noche";
    private static final LocalTime NOCHE_START = LocalTime.of(22, 0);
    private static final LocalTime NOCHE_END = LocalTime.of(6, 0);
    
    // Saturday shift: 09:00 - 13:00
    private static final String SABADO_NAME = "Sábado";
    private static final LocalTime SABADO_START = LocalTime.of(9, 0);
    private static final LocalTime SABADO_END = LocalTime.of(13, 0);
    
    // Sunday shift: 08:00 - 16:00
    private static final String DOMINGO_NAME = "Domingo";
    private static final LocalTime DOMINGO_START = LocalTime.of(8, 0);
    private static final LocalTime DOMINGO_END = LocalTime.of(16, 0);
    
    // Rest day
    private static final String DESCANSO_NAME = "Descanso";
    
    // Split shift (PARTIDO): Morning 07:00-13:00 + Afternoon 17:00-22:00
    private static final String PARTIDO_NAME = "Partido";
    private static final LocalTime PARTIDO_MORNING_START = LocalTime.of(7, 0);
    private static final LocalTime PARTIDO_MORNING_END = LocalTime.of(13, 0);
    private static final LocalTime PARTIDO_AFTERNOON_START = LocalTime.of(17, 0);
    private static final LocalTime PARTIDO_AFTERNOON_END = LocalTime.of(22, 0);
    
    private static final Map<String, String> SHIFT_NAME_MAPPING = new HashMap<>();
    
    static {
        SHIFT_NAME_MAPPING.put("MAÑANA", MAÑANA_NAME);
        SHIFT_NAME_MAPPING.put("MORNING", MAÑANA_NAME);
        SHIFT_NAME_MAPPING.put("TARDE", TARDE_NAME);
        SHIFT_NAME_MAPPING.put("AFTERNOON", TARDE_NAME);
        SHIFT_NAME_MAPPING.put("NOCHE", NOCHE_NAME);
        SHIFT_NAME_MAPPING.put("NIGHT", NOCHE_NAME);
        SHIFT_NAME_MAPPING.put("SÁBADO", SABADO_NAME);
        SHIFT_NAME_MAPPING.put("SABADO", SABADO_NAME);
        SHIFT_NAME_MAPPING.put("SATURDAY", SABADO_NAME);
        SHIFT_NAME_MAPPING.put("DOMINGO", DOMINGO_NAME);
        SHIFT_NAME_MAPPING.put("SUNDAY", DOMINGO_NAME);
        SHIFT_NAME_MAPPING.put("LIBRE", DESCANSO_NAME);
        SHIFT_NAME_MAPPING.put("REST", DESCANSO_NAME);
        SHIFT_NAME_MAPPING.put("PARTIDO", PARTIDO_NAME);
    }
    
    /**
     * Find or create a shift type based on the extracted name and times.
     * 
     * @param shiftName The name of the shift from the PDF
     * @param startTime The start time of the shift
     * @param endTime The end time of the shift
     * @return The matching or newly created ShiftType
     */
    public ShiftType findOrCreateShiftType(String shiftName, LocalTime startTime, LocalTime endTime) {
        if (shiftName == null || shiftName.trim().isEmpty()) {
            shiftName = determineShiftTypeByTime(startTime, endTime);
        }
        
        String normalizedName = normalizeShiftName(shiftName);
        String canonicalName = SHIFT_NAME_MAPPING.getOrDefault(normalizedName, shiftName);
        
        // Try to find by canonical name in database
        Optional<ShiftType> existing = shiftTypeRepository.findByNameIgnoreCase(canonicalName);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Also try with original name
        existing = shiftTypeRepository.findByNameIgnoreCase(shiftName);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Check for special multi-range shifts
        if (PARTIDO_NAME.equalsIgnoreCase(canonicalName) || "PARTIDO".equalsIgnoreCase(normalizedName)) {
            return createMultiRangeShiftType(PARTIDO_NAME, 
                "Turno partido - 7am a 1pm y 5pm a 10pm (8 horas)",
                PARTIDO_MORNING_START, PARTIDO_MORNING_END,
                PARTIDO_AFTERNOON_START, PARTIDO_AFTERNOON_END);
        }
        
        // Create new shift type
        return createNewShiftType(canonicalName, startTime, endTime);
    }
    
    /**
     * Create a multi-range shift type with two time ranges.
     * Used for PARTIDO type shifts.
     * 
     * @param name        Shift type name
     * @param description Shift type description
     * @param range1Start Start time of first range
     * @param range1End   End time of first range
     * @param range2Start Start time of second range
     * @param range2End   End time of second range
     * @return The created ShiftType
     */
    public ShiftType createMultiRangeShiftType(String name, String description,
            LocalTime range1Start, LocalTime range1End,
            LocalTime range2Start, LocalTime range2End) {
        
        // Check if it already exists
        Optional<ShiftType> existing = shiftTypeRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        ShiftType newShiftType = new ShiftType();
        newShiftType.setName(name);
        newShiftType.setDescription(description);
        newShiftType.setIsMultiRange(true);
        
        // Add time ranges
        newShiftType.addTimeRange(range1Start, range1End, 1);
        newShiftType.addTimeRange(range2Start, range2End, 2);
        
        // Set backward compatible fields from first range
        newShiftType.setStartTime(range1Start);
        newShiftType.setEndTime(range2End);
        newShiftType.setIsNightShift(false); // Neither range is night
        
        ShiftType saved = shiftTypeRepository.save(newShiftType);
        log.info("Created multi-range shift type: {} with ranges {}-{} and {}-{}", 
            name, range1Start, range1End, range2Start, range2End);
        
        return saved;
    }
    
    /**
     * Create a new shift type with the given parameters.
     */
    private ShiftType createNewShiftType(String name, LocalTime startTime, LocalTime endTime) {
        ShiftType newShiftType = new ShiftType();
        newShiftType.setName(name);
        
        if (startTime != null && endTime != null) {
            newShiftType.setStartTime(startTime);
            newShiftType.setEndTime(endTime);
            
            // Determine if it's a night shift
            int startHour = startTime.getHour();
            boolean isNight = startHour >= 22 || startHour < 6;
            newShiftType.setIsNightShift(isNight);
        } else {
            // For rest days, set default values
            newShiftType.setStartTime(MAÑANA_START);
            newShiftType.setEndTime(MAÑANA_END);
            newShiftType.setIsNightShift(false);
        }
        
        // Initialize time ranges list (empty for single-range)
        newShiftType.setTimeRanges(new ArrayList<>());
        newShiftType.setIsMultiRange(false);
        
        newShiftType.setDescription("Imported from PDF: " + name);
        
        ShiftType saved = shiftTypeRepository.save(newShiftType);
        log.info("Created new shift type from PDF: {}", name);
        
        return saved;
    }
    
    /**
     * Normalize shift name for comparison.
     */
    private String normalizeShiftName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toUpperCase()
                .replace("TURNO ", "")
                .replace("SHIFT ", "")
                .replace("DE ", " ")
                .trim();
    }
    
    /**
     * Determine shift type based on time range.
     */
    private String determineShiftTypeByTime(LocalTime start, LocalTime end) {
        if (start == null) {
            return "TURNO";
        }
        
        int startHour = start.getHour();
        
        if (startHour >= 6 && startHour < 14) {
            return MAÑANA_NAME;
        } else if (startHour >= 14 && startHour < 22) {
            return TARDE_NAME;
        } else if (startHour >= 22 || startHour < 6) {
            return NOCHE_NAME;
        }
        
        return "TURNO";
    }
    
    /**
     * Calculate total hours for a shift type.
     * For multi-range shifts, sums all time ranges.
     * For single-range shifts, uses the simple start-end calculation.
     * 
     * @param shiftType The shift type to calculate hours for
     * @return Total hours
     */
    public double calculateTotalHours(ShiftType shiftType) {
        if (shiftType == null) {
            return 0.0;
        }
        
        // Use the entity method if time ranges exist
        if (shiftType.getTimeRanges() != null && !shiftType.getTimeRanges().isEmpty()) {
            return shiftType.getTotalDurationHours();
        }
        
        // Fallback to simple calculation for backward compatibility
        return calculateSimpleHours(shiftType.getStartTime(), shiftType.getEndTime());
    }
    
    /**
     * Simple hours calculation (for backward compatibility).
     */
    private double calculateSimpleHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return 0.0;
        }
        
        // Handle night shift crossing midnight
        if (end.isBefore(start)) {
            int startToMidnight = 24 - start.getHour();
            int midnightToEnd = end.getHour();
            return startToMidnight + midnightToEnd;
        }
        
        return (end.getHour() - start.getHour()) + 
               (end.getMinute() - start.getMinute()) / 60.0;
    }
}
