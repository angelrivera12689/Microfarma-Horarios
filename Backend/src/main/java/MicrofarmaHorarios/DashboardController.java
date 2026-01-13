package MicrofarmaHorarios;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // Mock data - replace with actual service calls
        int activeUsers = 25;
        int employees = 18;
        int todaysShifts = 12;
        int notifications = 3;

        data.put("activeUsers", activeUsers);
        data.put("employees", employees);
        data.put("todaysShifts", todaysShifts);
        data.put("notifications", notifications);

        // Calculated metrics
        double efficiency = employees > 0 ? (double) activeUsers / employees * 100 : 0;
        double satisfaction = 4.8; // Mock satisfaction score
        double growth = 12.0; // Mock growth percentage

        data.put("efficiency", Math.round(efficiency * 100.0) / 100.0);
        data.put("satisfaction", satisfaction);
        data.put("growth", growth);

        // Weekly schedule data
        List<Map<String, String>> weeklySchedule = Arrays.asList(
            Map.of("day", "Lun", "hours", "8:00 - 17:00", "status", "active"),
            Map.of("day", "Mar", "hours", "8:00 - 17:00", "status", "active"),
            Map.of("day", "Mié", "hours", "8:00 - 17:00", "status", "active"),
            Map.of("day", "Jue", "hours", "8:00 - 17:00", "status", "active"),
            Map.of("day", "Vie", "hours", "8:00 - 17:00", "status", "active"),
            Map.of("day", "Sáb", "hours", "9:00 - 14:00", "status", "partial"),
            Map.of("day", "Dom", "hours", "8:00 - 17:00", "status", "active")
        );

        data.put("weeklySchedule", weeklySchedule);

        // Shifts per day data (mock)
        List<Map<String, Object>> shiftsPerDay = Arrays.asList(
            Map.of("day", "Lun", "shifts", 5),
            Map.of("day", "Mar", "shifts", 6),
            Map.of("day", "Mié", "shifts", 4),
            Map.of("day", "Jue", "shifts", 7),
            Map.of("day", "Vie", "shifts", 8),
            Map.of("day", "Sáb", "shifts", 3),
            Map.of("day", "Dom", "shifts", 2)
        );

        data.put("shiftsPerDay", shiftsPerDay);

        return ResponseEntity.ok(data);
    }
}