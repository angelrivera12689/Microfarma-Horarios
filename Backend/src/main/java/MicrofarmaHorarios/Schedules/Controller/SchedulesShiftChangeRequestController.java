package MicrofarmaHorarios.Schedules.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IService.IOrganizationLocationService;
import MicrofarmaHorarios.Schedules.DTO.Request.ShiftChangeDecisionDto;
import MicrofarmaHorarios.Schedules.DTO.Request.ShiftChangeRequestDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ShiftChangeRequestResponseDto;
import MicrofarmaHorarios.Schedules.Entity.RequestStatus;
import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.Entity.ShiftChangeRequest;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftChangeRequestService;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftService;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftTypeService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;

@RestController
@RequestMapping("/api/schedules/shift-change-requests")
public class SchedulesShiftChangeRequestController {

    @Autowired
    private ISchedulesShiftChangeRequestService shiftChangeRequestService;

    @Autowired
    private ISchedulesShiftService shiftService;

    @Autowired
    private ISchedulesShiftTypeService shiftTypeService;

    @Autowired
    private IOrganizationLocationService locationService;

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    @Autowired
    private ISecurityUserService userService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponseDto<ShiftChangeRequestResponseDto>> createRequest(@RequestBody ShiftChangeRequestDto requestDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDto<ShiftChangeRequestResponseDto>("Usuario no autenticado", null, false));
            }

            String email = authentication.getName();
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDto<ShiftChangeRequestResponseDto>("Usuario no autenticado", null, false));
            }

            var employeeOpt = employeeService.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<ShiftChangeRequestResponseDto>("Empleado no encontrado", null, false));
            }

            Employee employee = employeeOpt.get();
            Shift originalShift = shiftService.findById(requestDto.getOriginalShiftId()).orElseThrow(() -> new Exception("Turno no encontrado"));

            // Verify the shift belongs to the employee
            if (!originalShift.getEmployee().getId().equals(employee.getId())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<ShiftChangeRequestResponseDto>("No puedes solicitar cambios para turnos de otros empleados", null, false));
            }

            ShiftChangeRequest request = new ShiftChangeRequest();
            request.setEmployee(employee);
            request.setOriginalShift(originalShift);
            request.setRequestedDate(requestDto.getRequestedDate());
            if (requestDto.getRequestedShiftTypeId() != null) {
                ShiftType shiftType = shiftTypeService.findById(requestDto.getRequestedShiftTypeId()).orElse(null);
                request.setRequestedShiftType(shiftType);
            }
            if (requestDto.getRequestedLocationId() != null) {
                Location location = locationService.findById(requestDto.getRequestedLocationId()).orElse(null);
                request.setRequestedLocation(location);
            }
            request.setReason(requestDto.getReason());

            ShiftChangeRequest savedRequest = shiftChangeRequestService.createRequest(request);
            ShiftChangeRequestResponseDto response = mapToResponseDto(savedRequest);

            return ResponseEntity.ok(new ApiResponseDto<ShiftChangeRequestResponseDto>("Solicitud de cambio creada exitosamente", response, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<ShiftChangeRequestResponseDto>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponseDto<List<ShiftChangeRequestResponseDto>>> getMyRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDto<List<ShiftChangeRequestResponseDto>>("Usuario no autenticado", null, false));
            }

            String email = authentication.getName();
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDto<List<ShiftChangeRequestResponseDto>>("Usuario no autenticado", null, false));
            }

            var employeeOpt = employeeService.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<List<ShiftChangeRequestResponseDto>>("Empleado no encontrado", null, false));
            }

            Employee employee = employeeOpt.get();
            List<ShiftChangeRequest> requests = shiftChangeRequestService.findByEmployeeId(employee.getId());
            List<ShiftChangeRequestResponseDto> responses = requests.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponseDto<List<ShiftChangeRequestResponseDto>>("Mis solicitudes obtenidas", responses, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<ShiftChangeRequestResponseDto>>(e.getMessage(), null, false));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<List<ShiftChangeRequestResponseDto>>> getPendingRequests() {
        try {
            List<ShiftChangeRequest> requests = shiftChangeRequestService.findByRequestStatus(RequestStatus.PENDING);
            List<ShiftChangeRequestResponseDto> responses = requests.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponseDto<List<ShiftChangeRequestResponseDto>>("Solicitudes pendientes obtenidas", responses, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<ShiftChangeRequestResponseDto>>(e.getMessage(), null, false));
        }
    }

    @PutMapping("/{id}/decide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<ShiftChangeRequestResponseDto>> decideRequest(@PathVariable String id, @RequestBody ShiftChangeDecisionDto decisionDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDto<ShiftChangeRequestResponseDto>("Usuario no autenticado", null, false));
            }

            String email = authentication.getName();
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDto<ShiftChangeRequestResponseDto>("Usuario no autenticado", null, false));
            }

            var userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<ShiftChangeRequestResponseDto>("Usuario no encontrado", null, false));
            }

            String adminId = userOpt.get().getId();
            ShiftChangeRequest updatedRequest;

            if (decisionDto.getApproved()) {
                updatedRequest = shiftChangeRequestService.approveRequest(id, decisionDto.getAdminDecision(), adminId);
            } else {
                updatedRequest = shiftChangeRequestService.denyRequest(id, decisionDto.getAdminDecision(), adminId);
            }

            ShiftChangeRequestResponseDto response = mapToResponseDto(updatedRequest);

            return ResponseEntity.ok(new ApiResponseDto<ShiftChangeRequestResponseDto>("Decisión aplicada exitosamente", response, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<ShiftChangeRequestResponseDto>(e.getMessage(), null, false));
        }
    }

    private ShiftChangeRequestResponseDto mapToResponseDto(ShiftChangeRequest request) {
        ShiftChangeRequestResponseDto dto = new ShiftChangeRequestResponseDto();
        dto.setId(request.getId());

        if (request.getEmployee() != null) {
            dto.setEmployeeId(request.getEmployee().getId());
            String firstName = request.getEmployee().getFirstName() != null ? request.getEmployee().getFirstName() : "";
            String lastName = request.getEmployee().getLastName() != null ? request.getEmployee().getLastName() : "";
            dto.setEmployeeName(firstName + " " + lastName);
        }

        if (request.getOriginalShift() != null) {
            dto.setOriginalShiftId(request.getOriginalShift().getId());
            dto.setOriginalDate(request.getOriginalShift().getDate());
            if (request.getOriginalShift().getShiftType() != null) {
                dto.setOriginalShiftType(request.getOriginalShift().getShiftType().getName());
            }
            if (request.getOriginalShift().getLocation() != null) {
                dto.setOriginalLocation(request.getOriginalShift().getLocation().getName());
            }
        }

        dto.setRequestedDate(request.getRequestedDate());
        if (request.getRequestedShiftType() != null) {
            dto.setRequestedShiftType(request.getRequestedShiftType().getName());
        }
        if (request.getRequestedLocation() != null) {
            dto.setRequestedLocation(request.getRequestedLocation().getName());
        }
        dto.setReason(request.getReason());
        dto.setStatus(request.getRequestStatus());
        dto.setAdminDecision(request.getAdminDecision());
        if (request.getApprovedBy() != null) {
            dto.setApprovedByName(request.getApprovedBy().getName());
        }
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        return dto;
    }

}