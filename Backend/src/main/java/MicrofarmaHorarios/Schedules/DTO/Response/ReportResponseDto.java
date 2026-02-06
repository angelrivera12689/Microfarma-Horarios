package MicrofarmaHorarios.Schedules.DTO.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private GlobalReportDto global;
    private List<LocationReportDto> locations;
    private List<EmployeeReportDto> employees;
}