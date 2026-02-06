package MicrofarmaHorarios.Schedules.DTO.Request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class PdfScheduleDataDto {
    private String employeeCode;
    private String employeeName;
    private String locationName;
    private String locationId;
    private int month;
    private int year;
    private List<PdfShiftData> shifts;
    
    @Data
    public static class PdfShiftData {
        private LocalDate date;
        private String dayName;
        private LocalTime startTime;
        private LocalTime endTime;
        private String shiftTypeName;
        private boolean isRestDay;
    }
}
