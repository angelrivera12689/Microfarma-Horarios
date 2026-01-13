package MicrofarmaHorarios.HumanResources.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.HumanResources.Entity.EmployeeLocation;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeLocationService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@RestController
@RequestMapping("/api/humanresources/employeelocations")
public class HumanResourcesEmployeeLocationController extends AHumanResourcesBaseController<EmployeeLocation, IHumanResourcesEmployeeLocationService> {

    public HumanResourcesEmployeeLocationController(IHumanResourcesEmployeeLocationService service) {
        super(service, "EmployeeLocation");
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<List<EmployeeLocation>>> findByEmployeeId(@PathVariable String employeeId) {
        try {
            List<EmployeeLocation> employeeLocations = service.findByEmployeeId(employeeId);
            return ResponseEntity.ok(new ApiResponseDto<List<EmployeeLocation>>("Ubicaciones del empleado encontradas", employeeLocations, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<EmployeeLocation>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponseDto<List<EmployeeLocation>>> findByLocationId(@PathVariable String locationId) {
        try {
            List<EmployeeLocation> employeeLocations = service.findByLocationId(locationId);
            return ResponseEntity.ok(new ApiResponseDto<List<EmployeeLocation>>("Empleados en la ubicación encontrados", employeeLocations, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<EmployeeLocation>>(e.getMessage(), null, false));
        }
    }
}