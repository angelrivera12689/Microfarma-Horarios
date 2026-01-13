package MicrofarmaHorarios.Notification.DTO.Request;

import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import lombok.Data;

@Data
public class UpdateStatusRequestDto {
    private DeliveryStatus deliveryStatus;
    private String errorMessage;
}