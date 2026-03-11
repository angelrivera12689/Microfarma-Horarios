package MicrofarmaHorarios.Schedules.IService;

import java.util.List;

import MicrofarmaHorarios.Schedules.Entity.ShiftTimeRange;

/**
 * Service interface for ShiftTimeRange entity operations.
 */
public interface ISchedulesShiftTimeRangeService extends ISchedulesBaseService<ShiftTimeRange> {
    
    /**
     * Finds all time ranges for a specific shift type, ordered by range order.
     * 
     * @param shiftTypeId The ID of the shift type
     * @return List of time ranges ordered by rangeOrder
     */
    List<ShiftTimeRange> findByShiftTypeId(String shiftTypeId);
    
    /**
     * Saves multiple time ranges for a shift type.
     * This will replace any existing time ranges for the shift type.
     * 
     * @param timeRanges List of time ranges to save
     * @return List of saved time ranges
     */
    List<ShiftTimeRange> saveAll(List<ShiftTimeRange> timeRanges);
    
    /**
     * Deletes all time ranges for a specific shift type.
     * 
     * @param shiftTypeId The ID of the shift type
     */
    void deleteByShiftTypeId(String shiftTypeId);
}
