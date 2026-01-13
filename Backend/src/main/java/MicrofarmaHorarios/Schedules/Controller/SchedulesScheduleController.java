package MicrofarmaHorarios.Schedules.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.Schedules.Entity.Schedule;
import MicrofarmaHorarios.Schedules.IService.ISchedulesScheduleService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@RestController
@RequestMapping("/api/schedules/schedules")
public class SchedulesScheduleController extends ASchedulesBaseController<Schedule, ISchedulesScheduleService> {

    public SchedulesScheduleController(ISchedulesScheduleService service) {
        super(service, "Schedule");
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<List<Schedule>>> findByEmployeeId(@PathVariable String employeeId) {
        try {
            List<Schedule> schedules = service.findByEmployeeId(employeeId);
            return ResponseEntity.ok(new ApiResponseDto<List<Schedule>>("Horarios del empleado encontrados", schedules, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<Schedule>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/employee/{employeeId}/active")
    public ResponseEntity<ApiResponseDto<List<Schedule>>> findByEmployeeIdAndIsActiveTrue(@PathVariable String employeeId) {
        try {
            List<Schedule> schedules = service.findByEmployeeIdAndIsActiveTrue(employeeId);
            return ResponseEntity.ok(new ApiResponseDto<List<Schedule>>("Horarios activos del empleado encontrados", schedules, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<Schedule>>(e.getMessage(), null, false));
        }
    }
}