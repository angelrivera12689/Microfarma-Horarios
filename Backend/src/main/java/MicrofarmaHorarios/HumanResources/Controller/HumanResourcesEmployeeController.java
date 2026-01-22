package MicrofarmaHorarios.HumanResources.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/humanresources/employees")
@PreAuthorize("hasRole('ADMIN')")
public class HumanResourcesEmployeeController extends AHumanResourcesBaseController<Employee, IHumanResourcesEmployeeService> {

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    public HumanResourcesEmployeeController(IHumanResourcesEmployeeService service) {
        super(service, "Employee");
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponseDto<Optional<Employee>>> findByEmail(@PathVariable String email) {
        try {
            Optional<Employee> employee = employeeService.findByEmail(email);
            return ResponseEntity.ok(new ApiResponseDto<Optional<Employee>>("Empleado encontrado", employee, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<Optional<Employee>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Optional<Employee>>> getMyEmployee() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // Since UserDetails uses email as username
            Optional<Employee> employee = employeeService.findByEmail(email);
            return ResponseEntity.ok(new ApiResponseDto<Optional<Employee>>("Información del empleado obtenida", employee, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<Optional<Employee>>(e.getMessage(), null, false));
        }
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Employee>> updateMyEmployee(@RequestBody Employee employeeData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Optional<Employee> existingEmployeeOpt = employeeService.findByEmail(email);
            if (existingEmployeeOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<Employee>("Empleado no encontrado", null, false));
            }
            Employee existingEmployee = existingEmployeeOpt.get();

            // Update only allowed fields
            if (employeeData.getFirstName() != null) existingEmployee.setFirstName(employeeData.getFirstName());
            if (employeeData.getLastName() != null) existingEmployee.setLastName(employeeData.getLastName());
            if (employeeData.getPhone() != null) existingEmployee.setPhone(employeeData.getPhone());
            if (employeeData.getAddress() != null) existingEmployee.setAddress(employeeData.getAddress());
            if (employeeData.getBirthDate() != null) existingEmployee.setBirthDate(employeeData.getBirthDate());

            Employee updatedEmployee = employeeService.save(existingEmployee);
            return ResponseEntity.ok(new ApiResponseDto<Employee>("Perfil actualizado exitosamente", updatedEmployee, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<Employee>(e.getMessage(), null, false));
        }
    }
}