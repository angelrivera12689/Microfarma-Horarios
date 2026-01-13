package MicrofarmaHorarios.Schedules.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Schedules.Entity.Calendar;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesCalendarRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesCalendarService;

@Service
public class SchedulesCalendarService extends ASchedulesBaseService<Calendar> implements ISchedulesCalendarService {

    @Autowired
    private ISchedulesCalendarRepository calendarRepository;

    @Override
    protected ISchedulesBaseRepository<Calendar, String> getRepository() {
        return calendarRepository;
    }

}