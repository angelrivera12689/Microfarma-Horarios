package MicrofarmaHorarios.Schedules.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftTypeRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftTypeService;

@Service
public class SchedulesShiftTypeService extends ASchedulesBaseService<ShiftType> implements ISchedulesShiftTypeService {

    @Autowired
    private ISchedulesShiftTypeRepository shiftTypeRepository;

    @Override
    protected ISchedulesBaseRepository<ShiftType, String> getRepository() {
        return shiftTypeRepository;
    }

}