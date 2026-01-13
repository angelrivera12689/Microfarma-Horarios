package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Schedules.Entity.RequestStatus;
import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.Entity.ShiftChangeRequest;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftChangeRequestRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftChangeRequestService;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftService;
import MicrofarmaHorarios.Notification.Service.EmailService;
import MicrofarmaHorarios.Security.Entity.User;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;

@Service
public class SchedulesShiftChangeRequestService extends ASchedulesBaseService<ShiftChangeRequest> implements ISchedulesShiftChangeRequestService {

    @Autowired
    private ISchedulesShiftChangeRequestRepository shiftChangeRequestRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ISecurityUserService userService;

    @Autowired
    private ISchedulesShiftService shiftService;

    @Override
    protected ISchedulesBaseRepository<ShiftChangeRequest, String> getRepository() {
        return shiftChangeRequestRepository;
    }

    @Override
    public List<ShiftChangeRequest> findByEmployeeId(String employeeId) throws Exception {
        return shiftChangeRequestRepository.findByEmployee_Id(employeeId);
    }

    @Override
    public List<ShiftChangeRequest> findByRequestStatus(RequestStatus status) throws Exception {
        return shiftChangeRequestRepository.findByRequestStatus(status);
    }

    @Override
    public List<ShiftChangeRequest> findByEmployeeIdAndRequestStatus(String employeeId, RequestStatus status) throws Exception {
        return shiftChangeRequestRepository.findByEmployee_IdAndRequestStatus(employeeId, status);
    }

    @Override
    public ShiftChangeRequest createRequest(ShiftChangeRequest request) throws Exception {
        // Validation
        if (request.getOriginalShift() == null) {
            throw new Exception("Turno original es requerido");
        }

        // Check if shift is in the future
        if (request.getOriginalShift().getDate().isBefore(LocalDate.now())) {
            throw new Exception("No se pueden solicitar cambios para turnos pasados");
        }

        // Check if there's already a pending request for this shift
        List<ShiftChangeRequest> existingRequests = shiftChangeRequestRepository.findByEmployee_IdAndRequestStatus(
            request.getEmployee().getId(), RequestStatus.PENDING);
        boolean hasPendingForShift = existingRequests.stream()
            .anyMatch(r -> r.getOriginalShift().getId().equals(request.getOriginalShift().getId()));
        if (hasPendingForShift) {
            throw new Exception("Ya existe una solicitud pendiente para este turno");
        }

        // Validate that at least one change is requested
        if (request.getRequestedDate() == null &&
            request.getRequestedShiftType() == null &&
            request.getRequestedLocation() == null) {
            throw new Exception("Debe especificar al menos un cambio solicitado");
        }

        request.setRequestStatus(RequestStatus.PENDING);
        ShiftChangeRequest savedRequest = super.save(request);

        // Send notification email to admins
        try {
            emailService.sendShiftChangeRequestNotification(savedRequest);
        } catch (Exception e) {
            System.err.println("Error sending shift change request notification: " + e.getMessage());
        }

        return savedRequest;
    }

    @Override
    public ShiftChangeRequest approveRequest(String requestId, String adminDecision, String adminId) throws Exception {
        ShiftChangeRequest request = findById(requestId).orElseThrow(() -> new Exception("Solicitud no encontrada"));

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            throw new Exception("La solicitud no está en estado pendiente");
        }

        User admin = userService.findById(adminId).orElseThrow(() -> new Exception("Admin no encontrado"));

        request.setRequestStatus(RequestStatus.APPROVED);
        request.setAdminDecision(adminDecision);
        request.setApprovedBy(admin);

        // Update the original shift with requested changes
        Shift originalShift = request.getOriginalShift();
        if (request.getRequestedDate() != null) {
            originalShift.setDate(request.getRequestedDate());
        }
        if (request.getRequestedShiftType() != null) {
            originalShift.setShiftType(request.getRequestedShiftType());
        }
        if (request.getRequestedLocation() != null) {
            originalShift.setLocation(request.getRequestedLocation());
        }
        // Save the updated shift
        shiftService.save(originalShift);

        ShiftChangeRequest savedRequest = super.save(request);

        // Send notification email to employee
        try {
            emailService.sendShiftChangeRequestDecision(savedRequest);
        } catch (Exception e) {
            System.err.println("Error sending shift change decision notification: " + e.getMessage());
        }

        return savedRequest;
    }

    @Override
    public ShiftChangeRequest denyRequest(String requestId, String adminDecision, String adminId) throws Exception {
        ShiftChangeRequest request = findById(requestId).orElseThrow(() -> new Exception("Solicitud no encontrada"));

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            throw new Exception("La solicitud no está en estado pendiente");
        }

        User admin = userService.findById(adminId).orElseThrow(() -> new Exception("Admin no encontrado"));

        request.setRequestStatus(RequestStatus.DENIED);
        request.setAdminDecision(adminDecision);
        request.setApprovedBy(admin);

        ShiftChangeRequest savedRequest = super.save(request);

        // Send notification email to employee
        try {
            emailService.sendShiftChangeRequestDecision(savedRequest);
        } catch (Exception e) {
            System.err.println("Error sending shift change decision notification: " + e.getMessage());
        }

        return savedRequest;
    }

}