package MicrofarmaHorarios.Schedules.DTO.Response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfScheduleImportResultDto {
    private boolean success;
    private String message;
    private int totalShiftsImported;
    private int totalErrors;
    private List<String> errors;
    private List<ShiftImportDetailDto> importedShifts;
    
    public static PdfScheduleImportResultDto success(int totalImported, List<ShiftImportDetailDto> shifts) {
        return PdfScheduleImportResultDto.builder()
                .success(true)
                .message("Turnos importados exitosamente")
                .totalShiftsImported(totalImported)
                .totalErrors(0)
                .errors(null)
                .importedShifts(shifts)
                .build();
    }
    
    public static PdfScheduleImportResultDto error(String message, List<String> errors) {
        return PdfScheduleImportResultDto.builder()
                .success(false)
                .message(message)
                .totalShiftsImported(0)
                .totalErrors(errors.size())
                .errors(errors)
                .importedShifts(null)
                .build();
    }
}
