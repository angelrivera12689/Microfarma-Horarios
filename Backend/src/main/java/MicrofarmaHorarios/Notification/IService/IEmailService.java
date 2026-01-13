package MicrofarmaHorarios.Notification.IService;

import MicrofarmaHorarios.Notification.DTO.Request.EmailRequestDto;
import MicrofarmaHorarios.Security.Entity.User;

public interface IEmailService {

    void sendEmail(EmailRequestDto emailRequest) throws Exception;

    void sendPasswordResetEmail(User user, String token) throws Exception;
}