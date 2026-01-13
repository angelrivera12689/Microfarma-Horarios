package MicrofarmaHorarios.Schedules.IRepository;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Schedules.Entity.Calendar;

@Repository
public interface ISchedulesCalendarRepository extends ISchedulesBaseRepository<Calendar, String> {

}