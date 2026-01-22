package MicrofarmaHorarios.Schedules.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalReportDto {
    private Integer totalEmployees;
    private Double totalHours;
    private Double totalOvertimeHours;

    // Nuevos campos para categorización colombiana
    private Double totalRegularHours;
    private Double totalDiurnaExtraHours;
    private Double totalNocturnaExtraHours;
    private Double totalDominicalHours;
    private Double totalFestivoHours;
}