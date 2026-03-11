package MicrofarmaHorarios.Schedules.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Schedules.Entity.ShiftTimeRange;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftTimeRangeRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftTimeRangeService;

/**
 * Service implementation for ShiftTimeRange entity.
 * Provides CRUD operations and specific methods for managing time ranges within shift types.
 */
@Service
public class SchedulesShiftTimeRangeService extends ASchedulesBaseService<ShiftTimeRange> 
        implements ISchedulesShiftTimeRangeService {

    @Autowired
    private ISchedulesShiftTimeRangeRepository shiftTimeRangeRepository;

    @Override
    protected ISchedulesBaseRepository<ShiftTimeRange, String> getRepository() {
        return shiftTimeRangeRepository;
    }

    @Override
    public List<ShiftTimeRange> findByShiftTypeId(String shiftTypeId) {
        return shiftTimeRangeRepository.findByShiftTypeIdOrderByRangeOrderAsc(shiftTypeId);
    }

    @Override
    public List<ShiftTimeRange> saveAll(List<ShiftTimeRange> timeRanges) {
        return shiftTimeRangeRepository.saveAll(timeRanges);
    }

    @Override
    public void deleteByShiftTypeId(String shiftTypeId) {
        shiftTimeRangeRepository.deleteByShiftTypeId(shiftTypeId);
    }
}
