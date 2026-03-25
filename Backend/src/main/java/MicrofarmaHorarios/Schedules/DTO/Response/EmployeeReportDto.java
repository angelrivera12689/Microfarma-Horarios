package MicrofarmaHorarios.Schedules.DTO.Response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeReportDto {
    private String employeeId;
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

    // Número de turnos trabajados
    private Integer totalShifts;
    
    // Días laborados
    private Integer workingDays;
    
    // Position del empleado (para filtrar domiciliarios)
    private String position;
    private String positionName;
    
    // Locations donde trabaja el empleado
    private List<String> locations;
    
    // Constructor completo
    public EmployeeReportDto(String employeeId, String fullName, Double dailyAvgHours, 
            Double weeklyTotalHours, Double totalHours, Double overtimeHours, 
            List<OvertimeDetailDto> overtimeDetails, Double regularHours, 
            Double diurnaExtraHours, Double nocturnaExtraHours, Double dominicalHours, 
            Double festivoHours, Integer totalShifts, Integer workingDays,
            String position, String positionName, List<String> locations) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.dailyAvgHours = dailyAvgHours;
        this.weeklyTotalHours = weeklyTotalHours;
        this.totalHours = totalHours;
        this.overtimeHours = overtimeHours;
        this.overtimeDetails = overtimeDetails;
        this.regularHours = regularHours;
        this.diurnaExtraHours = diurnaExtraHours;
        this.nocturnaExtraHours = nocturnaExtraHours;
        this.dominicalHours = dominicalHours;
        this.festivoHours = festivoHours;
        this.totalShifts = totalShifts;
        this.workingDays = workingDays;
        this.position = position;
        this.positionName = positionName;
        this.locations = locations;
    }
    
    // Constructor sin position (para compatibilidad)
    public EmployeeReportDto(String employeeId, String fullName, Double dailyAvgHours, 
            Double weeklyTotalHours, Double totalHours, Double overtimeHours, 
            List<OvertimeDetailDto> overtimeDetails, Double regularHours, 
            Double diurnaExtraHours, Double nocturnaExtraHours, Double dominicalHours, 
            Double festivoHours, Integer totalShifts, Integer workingDays) {
        this(employeeId, fullName, dailyAvgHours, weeklyTotalHours, totalHours, overtimeHours,
            overtimeDetails, regularHours, diurnaExtraHours, nocturnaExtraHours, dominicalHours,
            festivoHours, totalShifts, workingDays, null, null, null);
    }
}