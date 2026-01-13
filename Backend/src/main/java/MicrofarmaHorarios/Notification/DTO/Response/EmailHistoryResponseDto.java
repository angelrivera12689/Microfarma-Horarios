package MicrofarmaHorarios.Notification.DTO.Response;

import java.time.LocalDateTime;

import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import lombok.Data;

@Data
public class EmailHistoryResponseDto {
    private String id;
    private String recipient;
    private String subject;
    private String body;
    private LocalDateTime sentAt;
    private DeliveryStatus deliveryStatus;
    private String errorMessage;
    private String emailType;
    private String referenceId;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}