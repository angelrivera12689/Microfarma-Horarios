package MicrofarmaHorarios.Schedules.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;

@RestController
@RequestMapping("/api/schedules/shifts")
public class SchedulesShiftController extends ASchedulesBaseController<Shift, ISchedulesShiftService> {

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    @Autowired
    private ISecurityUserService userService;

    public SchedulesShiftController(ISchedulesShiftService service) {
        super(service, "Shift");
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<List<Shift>>> findByEmployeeId(@PathVariable String employeeId) {
        try {
            List<Shift> shifts = service.findByEmployeeId(employeeId);
            return ResponseEntity.ok(new ApiResponseDto<List<Shift>>("Turnos del empleado encontrados", shifts, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<Shift>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponseDto<List<Shift>>> findByLocationId(@PathVariable String locationId) {
        try {
            List<Shift> shifts = service.findByLocationId(locationId);
            return ResponseEntity.ok(new ApiResponseDto<List<Shift>>("Turnos en la ubicación encontrados", shifts, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<Shift>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponseDto<List<Shift>>> findByDateBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Shift> shifts = service.findByDateBetween(startDate, endDate);
            return ResponseEntity.ok(new ApiResponseDto<List<Shift>>("Turnos en el rango de fechas encontrados", shifts, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<Shift>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/pdf/{year}/{month}")
    public ResponseEntity<byte[]> generateCalendarPdf(@PathVariable int year, @PathVariable int month,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String employeeId) {
        try {
            byte[] pdfBytes = service.generateCalendarPdf(year, month, locationId, employeeId);
            String filename = "calendario_turnos_" + year + "_" + String.format("%02d", month);
            if (locationId != null && !locationId.isEmpty()) {
                filename += "_sede";
            }
            if (employeeId != null && !employeeId.isEmpty()) {
                filename += "_empleado";
            }
            filename += ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<List<Shift>>> getMyShifts() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Find employee by email
            var employeeOpt = employeeService.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<List<Shift>>("No se encontró empleado con este email", null, false));
            }

            Employee employee = employeeOpt.get();
            List<Shift> shifts = service.findByEmployeeId(employee.getId());
            String message = "Mis turnos obtenidos para " + employee.getFirstName() + " " + employee.getLastName() + " (ID: " + employee.getId() + ")";
            return ResponseEntity.ok(new ApiResponseDto<List<Shift>>(message, shifts, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<Shift>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/me/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> generateMyShiftsPdf() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Find employee by email
            var employeeOpt = employeeService.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Employee employee = employeeOpt.get();
            byte[] pdfBytes = service.generatePersonalShiftsPdf(employee.getId());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "mis_turnos_" + employee.getFirstName() + "_" + employee.getLastName() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponseDto<List<Shift>>> saveAll(@RequestBody List<Shift> shifts) {
        try {
            List<Shift> savedShifts = service.saveAll(shifts);
            return ResponseEntity.ok(new ApiResponseDto<List<Shift>>("Turnos guardados en bulk", savedShifts, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponseDto<List<Shift>>(e.getMessage(), null, false));
        }
    }
}