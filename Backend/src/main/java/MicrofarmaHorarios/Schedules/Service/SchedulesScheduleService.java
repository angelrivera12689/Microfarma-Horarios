package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.Schedules.Entity.Schedule;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesScheduleRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesScheduleService;

@Service
public class SchedulesScheduleService extends ASchedulesBaseService<Schedule> implements ISchedulesScheduleService {

    @Autowired
    private ISchedulesScheduleRepository scheduleRepository;

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    @Override
    protected ISchedulesBaseRepository<Schedule, String> getRepository() {
        return scheduleRepository;
    }

    @Override
    public List<Schedule> findByStateTrue() throws Exception {
        return super.findByStateTrue().stream()
                .filter(schedule -> schedule.getEmployee() != null)
                .toList();
    }

    @Override
    public List<Schedule> findByEmployeeId(String employeeId) throws Exception {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        return scheduleRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<Schedule> findByEmployeeIdAndIsActiveTrue(String employeeId) throws Exception {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        return scheduleRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
    }

    @Override
    public Map<String, Object> getEmployeeStatistics(String employeeId) throws Exception {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        List<Schedule> schedules = findByEmployeeId(employeeId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSchedules", schedules.size());
        stats.put("activeSchedules", schedules.stream().filter(s -> s.isActive()).count());
        return stats;
    }

    @Override
    public Schedule createSchedule(String employeeId, LocalDate date) throws Exception {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        Optional<Employee> employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            throw new Exception("Employee not found");
        }
        Schedule schedule = new Schedule();
        schedule.setName("Weekly Schedule starting " + date);
        schedule.setDescription("Weekly schedule from Monday to Sunday");
        schedule.setEmployee(employeeOpt.get());
        schedule.setStartDate(date);
        schedule.setEndDate(date.plusDays(6));
        schedule.setIsActive(true);
        return save(schedule);
    }
}