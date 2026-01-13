package MicrofarmaHorarios.News.Controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.News.Entity.News;
import MicrofarmaHorarios.News.IService.INewsNewsService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

import java.util.Optional;

@RestController
@RequestMapping("/api/news/news")
public class NewsNewsController extends ANewsBaseController<News, INewsNewsService> {

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    public NewsNewsController(INewsNewsService service) {
        super(service, "News");
    }

    @GetMapping("/newstype/{newsTypeId}")
    public ResponseEntity<ApiResponseDto<List<News>>> findByNewsTypeId(@PathVariable String newsTypeId) {
        try {
            List<News> news = service.findByNewsTypeId(newsTypeId);
            return ResponseEntity.ok(new ApiResponseDto<List<News>>("Noticias encontradas", news, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<News>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<List<News>>> findByEmployeeId(@PathVariable String employeeId) {
        try {
            List<News> news = service.findByEmployeeId(employeeId);
            return ResponseEntity.ok(new ApiResponseDto<List<News>>("Noticias del empleado encontradas", news, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<News>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponseDto<List<News>>> findByPublicationDateBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<News> news = service.findByPublicationDateBetween(startDate, endDate);
            return ResponseEntity.ok(new ApiResponseDto<List<News>>("Noticias en el rango de fechas encontradas", news, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<News>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<List<News>>> getMyNews() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Optional<Employee> employeeOpt = employeeService.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<List<News>>("Empleado no encontrado", null, false));
            }
            Employee employee = employeeOpt.get();
            List<News> news = service.findByEmployeeId(employee.getId());
            return ResponseEntity.ok(new ApiResponseDto<List<News>>("Mis noticias obtenidas", news, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<News>>(e.getMessage(), null, false));
        }
    }
}