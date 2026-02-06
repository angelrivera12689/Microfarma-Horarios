package MicrofarmaHorarios.Schedules.DTO.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationReportDto {
    private String locationName;
    private Integer totalEmployees;
    private Double totalHours;
    private Double totalOvertimeHours;
    private Double totalRegularHours;
    private Double totalDiurnaExtraHours;
    private Double totalNocturnaExtraHours;
    private Double totalDominicalHours;
    private Double totalFestivoHours;
    private Integer totalShifts;
    private List<EmployeeReportDto> employeeReports;
}