package MicrofarmaHorarios.Notification.DTO.Request;

import java.time.LocalDateTime;

import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import lombok.Data;

@Data
public class EmailHistoryRequestDto {
    private String recipient;
    private String subject;
    private String body;
    private LocalDateTime sentAt;
    private DeliveryStatus deliveryStatus;
    private String errorMessage;
    private String emailType;
    private String referenceId;
}