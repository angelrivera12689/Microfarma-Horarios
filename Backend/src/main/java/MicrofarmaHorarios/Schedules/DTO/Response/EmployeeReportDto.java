package MicrofarmaHorarios.Schedules.DTO.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReportDto {
    private String fullName;
    private Double dailyAvgHours;
    private Double weeklyTotalHours;
    private Double totalHours;
    private Double overtimeHours;
    private List<OvertimeDetailDto> overtimeDetails;

    // Nuevos campos para categorización colombiana
    private Double regularHours;
    private Double diurnaExtraHours;
    private Double nocturnaExtraHours;
    private Double dominicalHours;
    private Double festivoHours;
}