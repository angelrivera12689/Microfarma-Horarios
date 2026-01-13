package MicrofarmaHorarios.Notification.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.Notification.DTO.Request.EmailHistoryFilterDto;
import MicrofarmaHorarios.Notification.DTO.Request.EmailHistoryRequestDto;
import MicrofarmaHorarios.Notification.DTO.Request.UpdateStatusRequestDto;
import MicrofarmaHorarios.Notification.DTO.Response.EmailHistoryResponseDto;
import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import MicrofarmaHorarios.Notification.Entity.EmailHistory;
import MicrofarmaHorarios.Notification.IService.IEmailHistoryService;
import MicrofarmaHorarios.Security.Controller.ASecurityBaseController;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/notification/email-history")
public class EmailHistoryController extends ASecurityBaseController<EmailHistory, IEmailHistoryService> {

    @Autowired
    private IEmailHistoryService emailHistoryService;

    public EmailHistoryController(IEmailHistoryService service) {
        super(service, "EmailHistory");
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponseDto<List<EmailHistory>>> findByFilters(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) String emailType,
            @RequestParam(required = false) DeliveryStatus deliveryStatus,
            @RequestParam(required = false) String sentAtFrom,
            @RequestParam(required = false) String sentAtTo) {
        try {
            // Parse dates if provided
            java.time.LocalDateTime from = sentAtFrom != null ? java.time.LocalDateTime.parse(sentAtFrom) : null;
            java.time.LocalDateTime to = sentAtTo != null ? java.time.LocalDateTime.parse(sentAtTo) : null;
            List<EmailHistory> results = emailHistoryService.findByFilters(recipient, emailType, deliveryStatus, from, to);
            return ResponseEntity.ok(new ApiResponseDto<List<EmailHistory>>("Datos obtenidos", results, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponseDto<List<EmailHistory>>(e.getMessage(), null, false));
        }
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponseDto<String>> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequestDto request) {
        try {
            emailHistoryService.updateDeliveryStatus(id, request.getDeliveryStatus(), request.getErrorMessage());
            return ResponseEntity.ok(new ApiResponseDto<String>("Status actualizado", null, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponseDto<String>(e.getMessage(), null, false));
        }
    }
}