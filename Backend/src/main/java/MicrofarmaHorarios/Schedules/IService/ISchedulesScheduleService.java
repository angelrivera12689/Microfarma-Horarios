package MicrofarmaHorarios.Schedules.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import MicrofarmaHorarios.Schedules.Entity.Schedule;

public interface ISchedulesScheduleService extends ISchedulesBaseService<Schedule> {

    List<Schedule> findByEmployeeId(String employeeId) throws Exception;

    List<Schedule> findByEmployeeIdAndIsActiveTrue(String employeeId) throws Exception;

    Map<String, Object> getEmployeeStatistics(String employeeId) throws Exception;

    Schedule createSchedule(String employeeId, LocalDate date) throws Exception;
}