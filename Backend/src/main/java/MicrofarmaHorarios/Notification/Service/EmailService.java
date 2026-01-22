package MicrofarmaHorarios.Notification.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Notification.DTO.Request.EmailRequestDto;
import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import MicrofarmaHorarios.Notification.Entity.EmailHistory;
import MicrofarmaHorarios.Notification.IService.IEmailHistoryService;
import MicrofarmaHorarios.Notification.IService.IEmailService;
import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.Security.Entity.User;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private IEmailHistoryService emailHistoryService;

    @Override
    public void sendEmail(EmailRequestDto emailRequest) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailRequest.getTo());
        message.setSubject(emailRequest.getSubject());
        message.setText(emailRequest.getBody());
        try {
            mailSender.send(message);
            EmailHistory history = new EmailHistory();
            history.setRecipient(emailRequest.getTo());
            history.setSubject(emailRequest.getSubject());
            history.setBody(emailRequest.getBody());
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.SENT);
            emailHistoryService.save(history);
        } catch (Exception e) {
            EmailHistory history = new EmailHistory();
            history.setRecipient(emailRequest.getTo());
            history.setSubject(emailRequest.getSubject());
            history.setBody(emailRequest.getBody());
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            emailHistoryService.save(history);
            throw e;
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) throws Exception {
        String subject = "Recuperación de contraseña - Microfarma Horarios";
        String body = "<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;} .token{color:#007bff;font-size:24px;font-weight:bold;}</style></head><body>" +
                      "<h2>Hola " + user.getName() + ",</h2>" +
                      "<p>Has solicitado recuperar tu contraseña. Usa el siguiente código para resetearla:</p>" +
                      "<div class='token'>" + token + "</div>" +
                      "<p>Este código expira en 15 minutos.</p>" +
                      "<p>Si no solicitaste esto, ignora este email.</p>" +
                      "<br><p>Saludos,<br>Equipo de Microfarma Horarios</p></body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true); // true indicates HTML

        try {
            mailSender.send(message);
            EmailHistory history = new EmailHistory();
            history.setRecipient(user.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.SENT);
            emailHistoryService.save(history);
        } catch (Exception e) {
            EmailHistory history = new EmailHistory();
            history.setRecipient(user.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            emailHistoryService.save(history);
            throw e;
        }
    }

    public void sendShiftAssignmentEmail(Shift shift) throws Exception {
        Employee employee = shift.getEmployee();
        if (employee.getEmail() == null || employee.getEmail().isEmpty()) {
            return; // No email to send
        }

        String subject = "Nuevo turno asignado - Microfarma Horarios";
        String body = "<!DOCTYPE html><html><head><style>" +
                      "body{font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;}" +
                      ".container{background:white;border-radius:10px;padding:20px;margin:0 auto;max-width:600px;box-shadow:0 0 10px rgba(0,0,0,0.1);}" +
                      ".header{background:#dc143c;color:white;padding:15px;text-align:center;border-radius:10px 10px 0 0;}" +
                      ".content{padding:20px;}" +
                      ".shift-info{background:#ffebee;padding:15px;border-radius:5px;margin:10px 0;border-left:4px solid #dc143c;}" +
                      ".footer{text-align:center;color:#666;margin-top:20px;}" +
                      "</style></head><body>" +
                      "<div class='container'>" +
                      "<div class='header'><h2>🏥 Nuevo Turno Asignado</h2></div>" +
                      "<div class='content'>" +
                      "<h3>Hola " + employee.getFirstName() + " " + employee.getLastName() + ",</h3>" +
                      "<p>Se te ha asignado un nuevo turno. Aquí están los detalles:</p>" +
                      "<div class='shift-info'>" +
                      "<strong>📅 Fecha:</strong> " + shift.getDate() + "<br>" +
                      "<strong>🏢 Ubicación:</strong> " + shift.getLocation().getName() + "<br>" +
                      "<strong>⏰ Tipo de Turno:</strong> " + shift.getShiftType().getName() + "<br>" +
                      "<strong>🕐 Horario:</strong> " + shift.getShiftType().getStartTime() + " - " + shift.getShiftType().getEndTime() + "<br>" +
                      (shift.getNotes() != null && !shift.getNotes().isEmpty() ? "<strong>📝 Notas:</strong> " + shift.getNotes() : "") +
                      "</div>" +
                      "<p>Por favor confirma tu asistencia o contacta a tu supervisor si tienes alguna duda.</p>" +
                      "</div>" +
                      "<div class='footer'>" +
                      "<p>Saludos,<br>Equipo de Microfarma Horarios</p>" +
                      "</div></div></body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(employee.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true);

        try {
            mailSender.send(message);
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.SENT);
            emailHistoryService.save(history);
        } catch (Exception e) {
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            emailHistoryService.save(history);
            throw e;
        }
    }

    public void sendShiftReminderEmail(Shift shift) throws Exception {
        Employee employee = shift.getEmployee();
        if (employee.getEmail() == null || employee.getEmail().isEmpty()) {
            return;
        }

        String subject = "Recordatorio de turno - Microfarma Horarios";
        String body = "<!DOCTYPE html><html><head><style>" +
                      "body{font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;}" +
                      ".container{background:white;border-radius:10px;padding:20px;margin:0 auto;max-width:600px;box-shadow:0 0 10px rgba(0,0,0,0.1);}" +
                      ".header{background:#ff9800;color:white;padding:15px;text-align:center;border-radius:10px 10px 0 0;}" +
                      ".content{padding:20px;}" +
                      ".shift-info{background:#fff3e0;padding:15px;border-radius:5px;margin:10px 0;border-left:4px solid #ff9800;}" +
                      ".footer{text-align:center;color:#666;margin-top:20px;}" +
                      "</style></head><body>" +
                      "<div class='container'>" +
                      "<div class='header'><h2>⏰ Recordatorio de Turno</h2></div>" +
                      "<div class='content'>" +
                      "<h3>Hola " + employee.getFirstName() + " " + employee.getLastName() + ",</h3>" +
                      "<p>Este es un recordatorio de tu turno programado para mañana:</p>" +
                      "<div class='shift-info'>" +
                      "<strong>📅 Fecha:</strong> " + shift.getDate() + "<br>" +
                      "<strong>🏢 Ubicación:</strong> " + shift.getLocation().getName() + "<br>" +
                      "<strong>⏰ Tipo de Turno:</strong> " + shift.getShiftType().getName() + "<br>" +
                      "<strong>🕐 Horario:</strong> " + shift.getShiftType().getStartTime() + " - " + shift.getShiftType().getEndTime() + "<br>" +
                      (shift.getNotes() != null && !shift.getNotes().isEmpty() ? "<strong>📝 Notas:</strong> " + shift.getNotes() : "") +
                      "</div>" +
                      "<p>Recuerda llegar a tiempo y usar el equipo de protección necesario.</p>" +
                      "</div>" +
                      "<div class='footer'>" +
                      "<p>Saludos,<br>Equipo de Microfarma Horarios</p>" +
                      "</div></div></body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(employee.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true);

        try {
            mailSender.send(message);
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.SENT);
            emailHistoryService.save(history);
        } catch (Exception e) {
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            emailHistoryService.save(history);
            throw e;
        }
    }

    public void sendWelcomeEmail(Employee employee) throws Exception {
        if (employee.getEmail() == null || employee.getEmail().isEmpty()) {
            return;
        }

        String subject = "Bienvenido a Microfarma Horarios";
        String body = "<!DOCTYPE html><html><head><style>" +
                      "body{font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;}" +
                      ".container{background:white;border-radius:10px;padding:20px;margin:0 auto;max-width:600px;box-shadow:0 0 10px rgba(0,0,0,0.1);}" +
                      ".header{background:#4caf50;color:white;padding:15px;text-align:center;border-radius:10px 10px 0 0;}" +
                      ".content{padding:20px;}" +
                      ".welcome-message{background:#e8f5e8;padding:15px;border-radius:5px;margin:10px 0;border-left:4px solid #4caf50;}" +
                      ".footer{text-align:center;color:#666;margin-top:20px;}" +
                      "</style></head><body>" +
                      "<div class='container'>" +
                      "<div class='header'><h2>🎉 ¡Bienvenido a Microfarma!</h2></div>" +
                      "<div class='content'>" +
                      "<h3>Hola " + employee.getFirstName() + " " + employee.getLastName() + ",</h3>" +
                      "<div class='welcome-message'>" +
                      "<p>¡Bienvenido al equipo de Microfarma! Estamos emocionados de tenerte con nosotros.</p>" +
                      "<p>Tu cuenta ha sido creada exitosamente en nuestro sistema de horarios.</p>" +
                      "</div>" +
                      "<p>Pronto recibirás información sobre tus turnos asignados. Si tienes alguna pregunta, no dudes en contactar a tu supervisor.</p>" +
                      "</div>" +
                      "<div class='footer'>" +
                      "<p>Saludos,<br>Equipo de Microfarma Horarios</p>" +
                      "</div></div></body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(employee.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true);

        try {
            mailSender.send(message);
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.SENT);
            emailHistoryService.save(history);
        } catch (Exception e) {
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            emailHistoryService.save(history);
            throw e;
        }
    }

    public void sendBirthdayEmail(Employee employee) throws Exception {
        if (employee.getEmail() == null || employee.getEmail().isEmpty()) {
            return;
        }

        String subject = "¡Feliz Cumpleaños! - Microfarma Horarios";
        String body = "<!DOCTYPE html><html><head><style>" +
                      "body{font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;}" +
                      ".container{background:white;border-radius:10px;padding:20px;margin:0 auto;max-width:600px;box-shadow:0 0 10px rgba(0,0,0,0.1);}" +
                      ".header{background:#ff69b4;color:white;padding:15px;text-align:center;border-radius:10px 10px 0 0;}" +
                      ".content{padding:20px;}" +
                      ".birthday-message{background:#fce4ec;padding:15px;border-radius:5px;margin:10px 0;border-left:4px solid #ff69b4;text-align:center;}" +
                      ".footer{text-align:center;color:#666;margin-top:20px;}" +
                      "</style></head><body>" +
                      "<div class='container'>" +
                      "<div class='header'><h2>🎂 ¡Feliz Cumpleaños!</h2></div>" +
                      "<div class='content'>" +
                      "<h3>¡Hola " + employee.getFirstName() + "!</h3>" +
                      "<div class='birthday-message'>" +
                      "<h3>🎉 ¡Feliz Cumpleaños! 🎉</h3>" +
                      "<p>El equipo de Microfarma te desea un día lleno de alegría y felicidad.</p>" +
                      "<p>¡Que tengas un año maravilloso!</p>" +
                      "</div>" +
                      "</div>" +
                      "<div class='footer'>" +
                      "<p>Saludos,<br>Equipo de Microfarma Horarios</p>" +
                      "</div></div></body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(employee.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true);

        try {
            mailSender.send(message);
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.SENT);
            emailHistoryService.save(history);
        } catch (Exception e) {
            EmailHistory history = new EmailHistory();
            history.setRecipient(employee.getEmail());
            history.setSubject(subject);
            history.setBody(body);
            history.setSentAt(LocalDateTime.now());
            history.setDeliveryStatus(DeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            emailHistoryService.save(history);
            throw e;
        }
    }
}