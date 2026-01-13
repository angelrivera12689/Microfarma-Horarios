package MicrofarmaHorarios.Notification.DTO.Request;

import lombok.Data;

@Data
public class EmailRequestDto {
    private String to;
    private String subject;
    private String body;
}