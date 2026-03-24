package MicrofarmaHorarios.Notification.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.Service.HumanResourcesEmployeeService;
import MicrofarmaHorarios.News.Service.AlertGenerationService;
import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.Service.SchedulesShiftService;

@Component
public class NotificationScheduler implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private SchedulesShiftService shiftService;

    @Autowired
    private HumanResourcesEmployeeService employeeService;

    @Autowired
    private AlertGenerationService alertGenerationService;

    // Send shift reminders 24 hours before (runs daily at 9 AM)
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendShiftReminders() {
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Shift> tomorrowShifts = shiftService.findByDateBetween(tomorrow, tomorrow);

            for (Shift shift : tomorrowShifts) {
                try {
                    emailService.sendShiftReminderEmail(shift);
                } catch (Exception e) {
                    // Log error but continue
                }
            }
        } catch (Exception e) {
            // Log error but continue
        }
    }

    // Send birthday emails (runs daily at 8 AM)
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendBirthdayEmails() {
        try {
            List<Employee> allEmployees = employeeService.all();
            LocalDate today = LocalDate.now();

            for (Employee employee : allEmployees) {
                if (employee.getBirthDate() != null &&
                    employee.getBirthDate().getMonth() == today.getMonth() &&
                    employee.getBirthDate().getDayOfMonth() == today.getDayOfMonth()) {
                    try {
                        emailService.sendBirthdayEmail(employee);
                    } catch (Exception e) {
                        // Log error but continue
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue
        }
    }

    // Send work anniversary emails (runs daily at 8 AM)
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendWorkAnniversaryEmails() {
        try {
            List<Employee> allEmployees = employeeService.all();
            LocalDate today = LocalDate.now();

            for (Employee employee : allEmployees) {
                if (employee.getHireDate() != null &&
                    employee.getHireDate().getMonth() == today.getMonth() &&
                    employee.getHireDate().getDayOfMonth() == today.getDayOfMonth()) {
                    try {
                        // You can create a specific method for work anniversary or reuse birthday email
                        emailService.sendBirthdayEmail(employee); // Reuse for now
                    } catch (Exception e) {
                        // Log error but continue
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue
        }
    }

    // Generate automatic news alerts for birthdays, contract expiring and expired (runs daily at 7 AM)
    @Scheduled(cron = "0 0 7 * * ?")
    public void generateNewsAlerts() {
        logger.info("=========================================================");
        logger.info("EJECUTANDO SCHEDULER DE ALERTAS AUTOMÁTICAS");
        logger.info("=========================================================");
        try {
            alertGenerationService.generateDailyAlerts();
        } catch (Exception e) {
            logger.error("ERROR AL GENERAR ALERTAS AUTOMÁTICAS: {}", e.getMessage(), e);
        }
    }

    // Run alerts on startup for testing/development
    @Override
    public void run(String... args) throws Exception {
        logger.info("=========================================================");
        logger.info("EJECUTANDO ALERTAS EN INICIO (MODO DESARROLLO)");
        logger.info("=========================================================");
        try {
            alertGenerationService.generateDailyAlerts();
        } catch (Exception e) {
            logger.error("ERROR AL GENERAR ALERTAS EN INICIO: {}", e.getMessage(), e);
        }
    }
}