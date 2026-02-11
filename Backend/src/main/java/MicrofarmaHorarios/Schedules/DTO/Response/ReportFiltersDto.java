package MicrofarmaHorarios.Schedules.DTO.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportFiltersDto {
    private List<LocationFilterOption> locations;
    private List<EmployeeFilterOption> employees;
    private List<YearOption> years;
    private List<StatusOption> statuses;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationFilterOption {
        private String id;
        private String name;
        private String address;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeFilterOption {
        private String id;
        private String fullName;
        private String email;
        private String position;
        private String locationId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearOption {
        private Integer year;
        private String label;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusOption {
        private String id;
        private String name;
        private String description;
    }
}
