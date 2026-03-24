package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MicrofarmaHorarios.Schedules.Entity.ShiftTimeRange;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftTimeRangeRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftTypeRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftTypeService;

@Service
public class SchedulesShiftTypeService extends ASchedulesBaseService<ShiftType> implements ISchedulesShiftTypeService {

    @Autowired
    private ISchedulesShiftTypeRepository shiftTypeRepository;
    
    @Autowired
    private ISchedulesShiftTimeRangeRepository shiftTimeRangeRepository;

    @Override
    protected ISchedulesBaseRepository<ShiftType, String> getRepository() {
        return shiftTypeRepository;
    }
    
    /**
     * Override update to properly handle time ranges.
     */
    @Override
    @Transactional
    public void update(String id, ShiftType entity) throws Exception {
        ShiftType existingShiftType = shiftTypeRepository.findById(id)
            .orElseThrow(() -> new Exception("Tipo de turno no encontrado"));
        
        if (existingShiftType.getDeletedAt() != null) {
            throw new Exception("Registro inhabilitado");
        }
        
        // Update basic properties
        if (entity.getName() != null) {
            existingShiftType.setName(entity.getName());
        }
        if (entity.getDescription() != null) {
            existingShiftType.setDescription(entity.getDescription());
        }
        if (entity.getStartTime() != null) {
            existingShiftType.setStartTime(entity.getStartTime());
        }
        if (entity.getEndTime() != null) {
            existingShiftType.setEndTime(entity.getEndTime());
        }
        if (entity.getIsNightShift() != null) {
            existingShiftType.setIsNightShift(entity.getIsNightShift());
        }
        if (entity.getIsMultiRange() != null) {
            existingShiftType.setIsMultiRange(entity.getIsMultiRange());
        }
        
        // Handle time ranges if provided
        if (entity.getTimeRanges() != null && !entity.getTimeRanges().isEmpty()) {
            // Delete existing time ranges
            shiftTimeRangeRepository.deleteByShiftTypeId(id);
            
            // Clear existing ranges and add new ones
            existingShiftType.clearTimeRanges();
            
            for (ShiftTimeRange range : entity.getTimeRanges()) {
                if (range.getStartTime() != null && range.getEndTime() != null) {
                    existingShiftType.addTimeRange(range.getStartTime(), range.getEndTime(), range.getRangeOrder());
                }
            }
            
            // Save new time ranges with the shift type reference
            List<ShiftTimeRange> rangesToSave = new ArrayList<>();
            for (ShiftTimeRange range : existingShiftType.getTimeRanges()) {
                range.setShiftType(existingShiftType);
                rangesToSave.add(range);
            }
            if (!rangesToSave.isEmpty()) {
                shiftTimeRangeRepository.saveAll(rangesToSave);
            }
        }
        
        shiftTypeRepository.save(existingShiftType);
    }
    
    /**
     * Saves a ShiftType with its time ranges.
     * This method handles the cascade save for time ranges.
     * 
     * @param shiftType The shift type to save
     * @return The saved shift type with time ranges
     */
    @Override
    public ShiftType save(ShiftType shiftType) throws Exception {
        try {
            shiftType.setStatus(true);
            
            // If there are time ranges, we need to handle them BEFORE saving the shift type
            // to avoid the cascade trying to save them with null shift_type_id
            List<ShiftTimeRange> originalTimeRanges = null;
            if (shiftType.getTimeRanges() != null && !shiftType.getTimeRanges().isEmpty()) {
                originalTimeRanges = new ArrayList<>(shiftType.getTimeRanges());
                // Clear the time ranges from the entity to avoid cascade issues
                shiftType.getTimeRanges().clear();
            }
            
            // Save the shift type first to get the ID
            ShiftType savedShiftType = shiftTypeRepository.save(shiftType);
            
            // Now add the time ranges with the proper reference
            if (originalTimeRanges != null && !originalTimeRanges.isEmpty()) {
                for (ShiftTimeRange range : originalTimeRanges) {
                    savedShiftType.addTimeRange(range.getStartTime(), range.getEndTime(), range.getRangeOrder());
                }
                shiftTypeRepository.save(savedShiftType);
            }
            
            return savedShiftType;
        } catch (Exception e) {
            throw new Exception("Error al guardar el tipo de turno: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new shift type with multiple time ranges.
     * 
     * @param name        Name of the shift type
     * @param description Description of the shift type
     * @param ranges      List of time ranges (each with startTime, endTime, rangeOrder)
     * @return The created shift type
     */
    public ShiftType createWithRanges(String name, String description, List<ShiftTimeRange> ranges) throws Exception {
        ShiftType shiftType = new ShiftType();
        shiftType.setName(name);
        shiftType.setDescription(description);
        shiftType.setTimeRanges(new ArrayList<>());
        
        // Process ranges
        for (ShiftTimeRange range : ranges) {
            shiftType.addTimeRange(range.getStartTime(), range.getEndTime(), range.getRangeOrder());
        }
        
        return save(shiftType);
    }
    
    /**
     * Creates a simple single-range shift type (for backward compatibility).
     * 
     * @param name        Name of the shift type
     * @param description Description of the shift type
     * @param startTime  Start time of the shift
     * @param endTime    End time of the shift
     * @param isNightShift Whether this is a night shift
     * @return The created shift type
     */
    public ShiftType createSimple(String name, String description, LocalTime startTime, 
                                   LocalTime endTime, boolean isNightShift) throws Exception {
        ShiftType shiftType = new ShiftType();
        shiftType.setName(name);
        shiftType.setDescription(description);
        shiftType.setStartTime(startTime);
        shiftType.setEndTime(endTime);
        shiftType.setIsNightShift(isNightShift);
        shiftType.setIsMultiRange(false);
        shiftType.setTimeRanges(new ArrayList<>());
        
        return save(shiftType);
    }
    
    /**
     * Updates time ranges for an existing shift type.
     * 
     * @param shiftTypeId ID of the shift type to update
     * @param ranges      New list of time ranges
     */
    public void updateTimeRanges(String shiftTypeId, List<ShiftTimeRange> ranges) throws Exception {
        ShiftType shiftType = findById(shiftTypeId).orElseThrow(() -> 
            new Exception("Tipo de turno no encontrado"));
        
        // Clear existing ranges
        shiftTimeRangeRepository.deleteByShiftTypeId(shiftTypeId);
        
        // Add new ranges
        shiftType.clearTimeRanges();
        for (ShiftTimeRange range : ranges) {
            shiftType.addTimeRange(range.getStartTime(), range.getEndTime(), range.getRangeOrder());
        }
        
        // Save ranges
        List<ShiftTimeRange> rangesToSave = new ArrayList<>();
        for (ShiftTimeRange range : shiftType.getTimeRanges()) {
            range.setShiftType(shiftType);
            rangesToSave.add(range);
        }
        shiftTimeRangeRepository.saveAll(rangesToSave);
        
        // Update shift type
        shiftTypeRepository.save(shiftType);
    }

}