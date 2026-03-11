package MicrofarmaHorarios.Schedules.IRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Schedules.Entity.ShiftTimeRange;

/**
 * Repository interface for ShiftTimeRange entity.
 * Provides methods to query time ranges associated with shift types.
 */
@Repository
public interface ISchedulesShiftTimeRangeRepository extends ISchedulesBaseRepository<ShiftTimeRange, String> {
    
    /**
     * Finds all time ranges for a specific shift type, ordered by range order.
     * 
     * @param shiftTypeId The ID of the shift type
     * @return List of time ranges ordered by rangeOrder
     */
    List<ShiftTimeRange> findByShiftTypeIdOrderByRangeOrderAsc(String shiftTypeId);
    
    /**
     * Finds a specific time range by shift type and order.
     * 
     * @param shiftTypeId The ID of the shift type
     * @param rangeOrder  The order of the time range
     * @return Optional containing the time range if found
     */
    Optional<ShiftTimeRange> findByShiftTypeIdAndRangeOrder(String shiftTypeId, Integer rangeOrder);
    
    /**
     * Deletes all time ranges for a specific shift type.
     * 
     * @param shiftTypeId The ID of the shift type
     */
    void deleteByShiftTypeId(String shiftTypeId);
}
