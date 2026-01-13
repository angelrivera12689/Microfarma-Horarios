package MicrofarmaHorarios.Schedules.IRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Schedules.Entity.Schedule;

@Repository
public interface ISchedulesScheduleRepository extends ISchedulesBaseRepository<Schedule, String> {

    List<Schedule> findByEmployeeId(String employeeId);

    List<Schedule> findByEmployeeIdAndIsActiveTrue(String employeeId);
}