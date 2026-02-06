package MicrofarmaHorarios.Schedules.DTO.Response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeDetailDto {
    private LocalDate date;
    private Double hours;
    private String justification;
    private String location;
}