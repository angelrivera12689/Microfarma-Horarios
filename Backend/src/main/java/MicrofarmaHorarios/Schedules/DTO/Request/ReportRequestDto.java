package MicrofarmaHorarios.Schedules.DTO.Request;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    private Integer month;
    private Integer year;
    private String locationId;
    private String employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String reportType; // 'general', 'location', 'employee'
}
