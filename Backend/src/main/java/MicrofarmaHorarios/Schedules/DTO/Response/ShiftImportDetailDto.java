package MicrofarmaHorarios.Schedules.DTO.Response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftImportDetailDto {
    private String id;
    private LocalDate date;
    private String employeeName;
    private String locationName;
    private String shiftTypeName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // "CREATED", "UPDATED", "SKIPPED"
}
