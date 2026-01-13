package MicrofarmaHorarios.Notification.DTO.Request;

import java.time.LocalDateTime;

import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import lombok.Data;

@Data
public class EmailHistoryFilterDto {
    private String recipient;
    private String emailType;
    private DeliveryStatus deliveryStatus;
    private LocalDateTime sentAtFrom;
    private LocalDateTime sentAtTo;
}