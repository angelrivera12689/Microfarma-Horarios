package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MicrofarmaHorarios.Schedules.DTO.Response.EmployeeReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.GlobalReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.LocationReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.OvertimeDetailDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportFiltersDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;
import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.Entity.ShiftTimeRange;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesReportService;

@Service
public class SchedulesReportService implements ISchedulesReportService {

    @Autowired
    private ISchedulesShiftRepository shiftRepository;

    @Autowired
    private SchedulesHolidayService holidayService;

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto generateReport(int month, int year) throws Exception {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Shift> shifts = shiftRepository.findByDateBetween(startDate, endDate).stream()
                .filter(shift -> shift.getStatus() != null && shift.getStatus() && shift.getEmployee() != null && shift.getShiftType() != null)
                .collect(Collectors.toList());

        Map<String, List<Shift>> shiftsByLocation = shifts.stream()
                .filter(shift -> shift.getLocation().getId() != null)
                .collect(Collectors.groupingBy(shift -> shift.getLocation().getId()));

        List<LocationReportDto> locationReports = new ArrayList<>();
        double globalTotalHours = 0;
        double globalTotalOvertime = 0;
        double globalRegularHours = 0;
        double globalDiurnaExtra = 0;
        double globalNocturnaExtra = 0;
        double globalDominical = 0;
        double globalFestivo = 0;
        int globalTotalShifts = 0;
        int globalTotalEmployees = 0;

        for (Map.Entry<String, List<Shift>> entry : shiftsByLocation.entrySet()) {
            List<Shift> locationShifts = entry.getValue();
            String locationName = locationShifts.get(0).getLocation().getName();

            Map<String, List<Shift>> shiftsByEmployee = locationShifts.stream()
                    .filter(shift -> shift.getEmployee().getId() != null)
                    .collect(Collectors.groupingBy(shift -> shift.getEmployee().getId()));

            List<EmployeeReportDto> employeeReports = new ArrayList<>();
            double locationTotalHours = 0;
            double locationTotalOvertime = 0;
            double locationRegularHours = 0;
            double locationDiurnaExtra = 0;
            double locationNocturnaExtra = 0;
            double locationDominical = 0;
            double locationFestivo = 0;
            int locationTotalShifts = 0;

            for (Map.Entry<String, List<Shift>> empEntry : shiftsByEmployee.entrySet()) {
                List<Shift> employeeShifts = empEntry.getValue();
                EmployeeReportDto report = calculateEmployeeReport(employeeShifts);

                if (report != null) {
                    employeeReports.add(report);
                    locationTotalHours += report.getTotalHours();
                    locationTotalOvertime += report.getOvertimeHours();
                    locationRegularHours += report.getRegularHours() != null ? report.getRegularHours() : 0;
                    locationDiurnaExtra += report.getDiurnaExtraHours() != null ? report.getDiurnaExtraHours() : 0;
                    locationNocturnaExtra += report.getNocturnaExtraHours() != null ? report.getNocturnaExtraHours() : 0;
                    locationDominical += report.getDominicalHours() != null ? report.getDominicalHours() : 0;
                    locationFestivo += report.getFestivoHours() != null ? report.getFestivoHours() : 0;
                    locationTotalShifts += report.getTotalShifts() != null ? report.getTotalShifts() : 0;
                }
            }

            LocationReportDto locationReport = new LocationReportDto(
                    locationName,
                    shiftsByEmployee.size(),
                    locationTotalHours,
                    locationTotalOvertime,
                    locationRegularHours,
                    locationDiurnaExtra,
                    locationNocturnaExtra,
                    locationDominical,
                    locationFestivo,
                    locationTotalShifts,
                    employeeReports
            );
            locationReports.add(locationReport);

            globalTotalEmployees += shiftsByEmployee.size();
            globalTotalHours += locationTotalHours;
            globalTotalOvertime += locationTotalOvertime;
            globalRegularHours += locationRegularHours;
            globalDiurnaExtra += locationDiurnaExtra;
            globalNocturnaExtra += locationNocturnaExtra;
            globalDominical += locationDominical;
            globalFestivo += locationFestivo;
            globalTotalShifts += locationTotalShifts;
        }

        // Collect all employees from all shifts by unique employee
        Map<String, List<Shift>> shiftsByEmployeeAll = shifts.stream()
                .filter(shift -> shift.getEmployee() != null && shift.getEmployee().getId() != null)
                .collect(Collectors.groupingBy(shift -> shift.getEmployee().getId()));

        List<EmployeeReportDto> allEmployees = shiftsByEmployeeAll.entrySet().stream()
                .map(entry -> calculateEmployeeReport(entry.getValue()))
                .filter(report -> report != null)
                .collect(Collectors.toList());

        GlobalReportDto global = new GlobalReportDto(
                allEmployees.size(),
                globalTotalHours,
                globalTotalOvertime,
                globalRegularHours,
                globalDiurnaExtra,
                globalNocturnaExtra,
                globalDominical,
                globalFestivo,
                globalTotalShifts
        );
        return new ReportResponseDto(global, locationReports, allEmployees);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto generateReportByLocation(int month, int year, String locationId) throws Exception {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Shift> shifts = shiftRepository.findByDateBetweenAndLocationId(startDate, endDate, locationId).stream()
                .filter(shift -> shift.getStatus() != null && shift.getStatus() && shift.getEmployee() != null && shift.getShiftType() != null)
                .collect(Collectors.toList());

        Map<String, List<Shift>> shiftsByEmployee = shifts.stream()
                .filter(shift -> shift.getEmployee().getId() != null)
                .collect(Collectors.groupingBy(shift -> shift.getEmployee().getId()));

        List<EmployeeReportDto> employeeReports = new ArrayList<>();
        double locationTotalHours = 0;
        double locationTotalOvertime = 0;
        double locationRegularHours = 0;
        double locationDiurnaExtra = 0;
        double locationNocturnaExtra = 0;
        double locationDominical = 0;
        double locationFestivo = 0;
        int locationTotalShifts = 0;

        for (Map.Entry<String, List<Shift>> entry : shiftsByEmployee.entrySet()) {
            List<Shift> employeeShifts = entry.getValue();
            EmployeeReportDto report = calculateEmployeeReport(employeeShifts);

            if (report != null) {
                employeeReports.add(report);
                locationTotalHours += report.getTotalHours();
                locationTotalOvertime += report.getOvertimeHours();
                locationRegularHours += report.getRegularHours() != null ? report.getRegularHours() : 0;
                locationDiurnaExtra += report.getDiurnaExtraHours() != null ? report.getDiurnaExtraHours() : 0;
                locationNocturnaExtra += report.getNocturnaExtraHours() != null ? report.getNocturnaExtraHours() : 0;
                locationDominical += report.getDominicalHours() != null ? report.getDominicalHours() : 0;
                locationFestivo += report.getFestivoHours() != null ? report.getFestivoHours() : 0;
                locationTotalShifts += report.getTotalShifts() != null ? report.getTotalShifts() : 0;
            }
        }

        String locationName = shifts.isEmpty() ? "Unknown" : shifts.get(0).getLocation().getName();
        LocationReportDto locationReport = new LocationReportDto(
                locationName,
                shiftsByEmployee.size(),
                locationTotalHours,
                locationTotalOvertime,
                locationRegularHours,
                locationDiurnaExtra,
                locationNocturnaExtra,
                locationDominical,
                locationFestivo,
                locationTotalShifts,
                employeeReports
        );

        GlobalReportDto global = new GlobalReportDto(
                shiftsByEmployee.size(),
                locationTotalHours,
                locationTotalOvertime,
                locationRegularHours,
                locationDiurnaExtra,
                locationNocturnaExtra,
                locationDominical,
                locationFestivo,
                locationTotalShifts
        );
        return new ReportResponseDto(global, Arrays.asList(locationReport), employeeReports);
    }

    private EmployeeReportDto calculateEmployeeReport(List<Shift> shifts) {
        if (shifts.isEmpty()) {
            return null;
        }

        double totalHours = 0;
        double overtimeHours = 0;
        List<OvertimeDetailDto> overtimeDetails = new ArrayList<>();
        int workingDays = (int) shifts.stream().map(Shift::getDate).distinct().count();
        int daysInMonth = shifts.get(0).getDate().lengthOfMonth();
        double weeksInMonth = daysInMonth / 7.0;

        // Nuevas variables para categorización
        double regularHours = 0;
        double diurnaExtraHours = 0;
        double nocturnaExtraHours = 0;
        double dominicalHours = 0;
        double festivoHours = 0;

        Map<LocalDate, Integer> dailyRegularMinutes = new java.util.HashMap<>();

        for (Shift shift : shifts) {
            if (shift.getShiftType() == null) continue;

            double hours = shift.getShiftType().getTimeRanges() != null && 
                    !shift.getShiftType().getTimeRanges().isEmpty()
                    ? shift.getShiftType().getTotalDurationHours()
                    : calculateHours(shift.getShiftType().getStartTime(), shift.getShiftType().getEndTime());
            totalHours += hours;

            List<ShiftSegment> segments = splitShiftIntoSegments(shift);
            double overtimeForShift = 0.0;

            for (ShiftSegment segment : segments) {
                int durationMinutes = segment.getDurationMinutes();
                int usedRegular = dailyRegularMinutes.getOrDefault(segment.getDate(), 0);
                int availableRegular = Math.max(0, 8 * 60 - usedRegular);
                int regularMinutes = Math.min(durationMinutes, availableRegular);
                int overtimeMinutes = durationMinutes - regularMinutes;

                dailyRegularMinutes.put(segment.getDate(), usedRegular + regularMinutes);

                if (regularMinutes > 0) {
                    boolean isSunday = segment.getDate().getDayOfWeek().getValue() == 7;
                    boolean isHoliday = holidayService.isHoliday(segment.getDate());
                    if (isSunday) {
                        dominicalHours += regularMinutes / 60.0;
                    } else if (isHoliday) {
                        festivoHours += regularMinutes / 60.0;
                    } else {
                        regularHours += regularMinutes / 60.0;
                    }
                }

                if (overtimeMinutes > 0) {
                    overtimeForShift += overtimeMinutes / 60.0;
                    LocalTime overtimeStart = segment.getStart().plusMinutes(regularMinutes);
                    LocalTime overtimeEnd = segment.getEnd();
                    double diurnaExtra = calculateOverlapHours(overtimeStart, overtimeEnd,
                            LocalTime.of(6, 0), LocalTime.of(19, 0));
                    double nocturnaExtra = (overtimeMinutes / 60.0) - diurnaExtra;
                    diurnaExtraHours += diurnaExtra;
                    nocturnaExtraHours += nocturnaExtra;
                }
            }

            if (overtimeForShift > 0) {
                overtimeHours += overtimeForShift;
                overtimeDetails.add(new OvertimeDetailDto(
                        shift.getDate(),
                        overtimeForShift,
                        shift.getNotes(),
                        shift.getLocation() != null ? shift.getLocation().getName() : null));
            }
        }

        double dailyAvg = workingDays > 0 ? totalHours / workingDays : 0;
        double weeklyTotal = totalHours / weeksInMonth;

        String employeeId = shifts.get(0).getEmployee().getId();
        String firstName = shifts.get(0).getEmployee().getFirstName() != null ? shifts.get(0).getEmployee().getFirstName() : "";
        String lastName = shifts.get(0).getEmployee().getLastName() != null ? shifts.get(0).getEmployee().getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        
        // Obtener posición del empleado
        String positionName = shifts.get(0).getEmployee().getPosition() != null ? 
            shifts.get(0).getEmployee().getPosition().getName() : null;
        
        // Obtener ubicaciones donde trabaja el empleado
        List<String> employeeLocations = shifts.stream()
            .map(shift -> shift.getLocation() != null ? shift.getLocation().getName() : null)
            .filter(name -> name != null)
            .distinct()
            .collect(Collectors.toList());

        return new EmployeeReportDto(
                employeeId,
                fullName,
                dailyAvg,
                weeklyTotal,
                totalHours,
                overtimeHours,
                overtimeDetails,
                regularHours,
                diurnaExtraHours,
                nocturnaExtraHours,
                dominicalHours,
                festivoHours,
                shifts.size(),
                workingDays,
                positionName,
                positionName,
                employeeLocations
        );
    }

    private double calculateHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return 0.0;
        }
        int startSeconds = start.toSecondOfDay();
        int endSeconds = end.toSecondOfDay();
        if (endSeconds < startSeconds) {
            // Overnight shift
            endSeconds += 24 * 3600;
        }
        return (endSeconds - startSeconds) / 3600.0;
    }

    /**
     * Calcula las horas extras diurnas (6:00 AM - 7:00 PM) en un turno
     * Soporta turnos multi-rango (PARTIDO)
     */
    private static class ShiftSegment {
        private final LocalDate date;
        private final LocalTime start;
        private final LocalTime end;

        public ShiftSegment(LocalDate date, LocalTime start, LocalTime end) {
            this.date = date;
            this.start = start;
            this.end = end;
        }

        public LocalDate getDate() {
            return date;
        }

        public LocalTime getStart() {
            return start;
        }

        public LocalTime getEnd() {
            return end;
        }

        public int getDurationMinutes() {
            int startMinutes = start.getHour() * 60 + start.getMinute();
            int endMinutes = end.getHour() * 60 + end.getMinute();
            if (endMinutes < startMinutes) {
                return (24 * 60 - startMinutes) + endMinutes;
            }
            return endMinutes - startMinutes;
        }
    }

    private List<ShiftSegment> splitShiftIntoSegments(Shift shift) {
        List<ShiftSegment> segments = new ArrayList<>();
        if (shift == null || shift.getShiftType() == null) {
            return segments;
        }

        if (shift.getShiftType().getTimeRanges() != null && !shift.getShiftType().getTimeRanges().isEmpty()) {
            for (ShiftTimeRange range : shift.getShiftType().getTimeRanges()) {
                appendShiftSegment(segments, shift.getDate(), range.getStartTime(), range.getEndTime());
            }
        } else {
            appendShiftSegment(segments, shift.getDate(), shift.getShiftType().getStartTime(), shift.getShiftType().getEndTime());
        }
        return segments;
    }

    private void appendShiftSegment(List<ShiftSegment> segments, LocalDate baseDate, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return;
        }

        if (end.isBefore(start)) {
            segments.add(new ShiftSegment(baseDate, start, LocalTime.MIDNIGHT));
            segments.add(new ShiftSegment(baseDate.plusDays(1), LocalTime.MIDNIGHT, end));
        } else {
            segments.add(new ShiftSegment(baseDate, start, end));
        }
    }

    private double calculateOverlapHours(LocalTime start, LocalTime end, LocalTime windowStart, LocalTime windowEnd) {
        int startMinutes = toMinutes(start);
        int endMinutes = toMinutes(end);
        int windowStartMinutes = toMinutes(windowStart);
        int windowEndMinutes = toMinutes(windowEnd);

        int overlapMinutes = 0;
        for (int[] segment : toSegments(startMinutes, endMinutes)) {
            for (int[] windowSegment : toSegments(windowStartMinutes, windowEndMinutes)) {
                int begin = Math.max(segment[0], windowSegment[0]);
                int finish = Math.min(segment[1], windowSegment[1]);
                overlapMinutes += Math.max(0, finish - begin);
            }
        }
        return overlapMinutes / 60.0;
    }

    private int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private List<int[]> toSegments(int startMinutes, int endMinutes) {
        if (endMinutes < startMinutes) {
            List<int[]> segments = new ArrayList<>();
            segments.add(new int[] { startMinutes, 24 * 60 });
            segments.add(new int[] { 0, endMinutes });
            return segments;
        }
        List<int[]> segments = new ArrayList<>();
        segments.add(new int[] { startMinutes, endMinutes });
        return segments;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeReportDto generateEmployeeIndividualReport(int month, int year, String employeeId) throws Exception {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Shift> shifts = shiftRepository.findByDateBetween(startDate, endDate).stream()
                .filter(shift -> shift.getStatus() != null && shift.getStatus())
                .filter(shift -> shift.getEmployee() != null && shift.getEmployee().getId().equals(employeeId))
                .filter(shift -> shift.getShiftType() != null)
                .collect(Collectors.toList());

        return calculateEmployeeReport(shifts);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportFiltersDto getAvailableFilters() throws Exception {
        List<Shift> allShifts = shiftRepository.findAll();
        
        List<ReportFiltersDto.LocationFilterOption> locations = allShifts.stream()
                .filter(shift -> shift.getLocation() != null && shift.getLocation().getName() != null)
                .map(shift -> new ReportFiltersDto.LocationFilterOption(
                        shift.getLocation().getId(),
                        shift.getLocation().getName(),
                        shift.getLocation().getAddress()))
                .distinct()
                .collect(Collectors.toList());
        
        List<ReportFiltersDto.EmployeeFilterOption> employees = allShifts.stream()
                .filter(shift -> shift.getEmployee() != null && shift.getEmployee().getId() != null)
                .collect(Collectors.groupingBy(shift -> shift.getEmployee().getId(), Collectors.toList()))
                .values().stream()
                .map(shiftsByEmployee -> {
                    Employee employee = shiftsByEmployee.get(0).getEmployee();
                    return new ReportFiltersDto.EmployeeFilterOption(
                        employee.getId(),
                        employee.getFirstName() + " " + employee.getLastName(),
                        employee.getEmail(),
                        employee.getPosition() != null ? employee.getPosition().getName() : null,
                        shiftsByEmployee.get(0).getLocation() != null ? shiftsByEmployee.get(0).getLocation().getId() : null);
                })
                .collect(Collectors.toList());
        
        List<Integer> years = allShifts.stream()
                .map(shift -> shift.getDate().getYear())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<ReportFiltersDto.YearOption> yearOptions = years.stream()
                .map(year -> new ReportFiltersDto.YearOption(year, String.valueOf(year)))
                .collect(Collectors.toList());
        
        List<ReportFiltersDto.StatusOption> statuses = Arrays.asList(
                new ReportFiltersDto.StatusOption("1", "Activo", "Turno activo"),
                new ReportFiltersDto.StatusOption("0", "Inactivo", "Turno inactivo")
        );
        
        return new ReportFiltersDto(locations, employees, yearOptions, statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationReportDto> generateLocationReport(int month, int year, String locationId) throws Exception {
        ReportResponseDto report = generateReportByLocation(month, year, locationId);
        return report.getLocations();
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalReportDto generateGlobalReport(int month, int year) throws Exception {
        ReportResponseDto report = generateReport(month, year);
        return report.getGlobal();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto generateReportByEmployee(int month, int year, String employeeId) throws Exception {
        EmployeeReportDto employeeReport = generateEmployeeIndividualReport(month, year, employeeId);
        
        if (employeeReport == null) {
            String firstName = "";
            String lastName = "";
            employeeReport = new EmployeeReportDto();
            employeeReport.setEmployeeId(employeeId);
            employeeReport.setFullName(firstName + " " + lastName);
            employeeReport.setTotalHours(0.0);
            employeeReport.setOvertimeHours(0.0);
            employeeReport.setRegularHours(0.0);
            employeeReport.setDiurnaExtraHours(0.0);
            employeeReport.setNocturnaExtraHours(0.0);
            employeeReport.setDominicalHours(0.0);
            employeeReport.setFestivoHours(0.0);
            employeeReport.setTotalShifts(0);
            employeeReport.setWorkingDays(0);
            employeeReport.setPosition(null);
            employeeReport.setPositionName(null);
            employeeReport.setLocations(null);
        }
        
        GlobalReportDto global = new GlobalReportDto(
                1,
                employeeReport.getTotalHours(),
                employeeReport.getOvertimeHours(),
                employeeReport.getRegularHours() != null ? employeeReport.getRegularHours() : 0,
                employeeReport.getDiurnaExtraHours() != null ? employeeReport.getDiurnaExtraHours() : 0,
                employeeReport.getNocturnaExtraHours() != null ? employeeReport.getNocturnaExtraHours() : 0,
                employeeReport.getDominicalHours() != null ? employeeReport.getDominicalHours() : 0,
                employeeReport.getFestivoHours() != null ? employeeReport.getFestivoHours() : 0,
                employeeReport.getTotalShifts() != null ? employeeReport.getTotalShifts() : 0
        );
        
        return new ReportResponseDto(global, new ArrayList<>(), Arrays.asList(employeeReport));
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto generateDeliveryReport(int month, int year) throws Exception {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Helper to check if location is for delivery
        java.util.function.Function<String, Boolean> isDeliveryLocation = (locationName) -> {
            if (locationName == null) return false;
            String name = locationName.toLowerCase();
            return name.contains("zona norte") || name.contains("oriente") || name.contains("sur");
        };
        
        // Helper to check if employee is domiciliario
        java.util.function.Function<MicrofarmaHorarios.HumanResources.Entity.Employee, Boolean> isDomiciliario = (employee) -> {
            if (employee == null || employee.getPosition() == null || employee.getPosition().getName() == null) {
                return false;
            }
            return employee.getPosition().getName().toLowerCase().contains("domicili");
        };

        List<Shift> shifts = shiftRepository.findByDateBetween(startDate, endDate).stream()
                .filter(shift -> shift.getStatus() != null && shift.getStatus() && shift.getEmployee() != null && shift.getShiftType() != null)
                // Filter by delivery locations
                .filter(shift -> shift.getLocation() != null && isDeliveryLocation.apply(shift.getLocation().getName()))
                // Filter by domiciliario employees
                .filter(shift -> isDomiciliario.apply(shift.getEmployee()))
                .collect(Collectors.toList());

        Map<String, List<Shift>> shiftsByLocation = shifts.stream()
                .filter(shift -> shift.getLocation().getId() != null)
                .collect(Collectors.groupingBy(shift -> shift.getLocation().getId()));

        List<LocationReportDto> locationReports = new ArrayList<>();
        double globalTotalHours = 0;
        double globalTotalOvertime = 0;
        int globalTotalShifts = 0;
        int globalTotalEmployees = 0;

        for (Map.Entry<String, List<Shift>> entry : shiftsByLocation.entrySet()) {
            List<Shift> locationShifts = entry.getValue();
            String locationName = locationShifts.get(0).getLocation().getName();

            Map<String, List<Shift>> shiftsByEmployee = locationShifts.stream()
                    .filter(shift -> shift.getEmployee().getId() != null)
                    .collect(Collectors.groupingBy(shift -> shift.getEmployee().getId()));

            List<EmployeeReportDto> employeeReports = new ArrayList<>();
            double locationTotalHours = 0;
            double locationTotalOvertime = 0;
            int locationTotalShifts = 0;

            for (Map.Entry<String, List<Shift>> empEntry : shiftsByEmployee.entrySet()) {
                List<Shift> employeeShifts = empEntry.getValue();
                EmployeeReportDto report = calculateEmployeeReport(employeeShifts);

                if (report != null) {
                    employeeReports.add(report);
                    locationTotalHours += report.getTotalHours();
                    locationTotalOvertime += report.getOvertimeHours();
                    locationTotalShifts += report.getTotalShifts() != null ? report.getTotalShifts() : 0;
                }
            }

            LocationReportDto locationReport = new LocationReportDto(
                    locationName,
                    shiftsByEmployee.size(),
                    locationTotalHours,
                    locationTotalOvertime,
                    0.0,  // regularHours
                    0.0,  // diurnaExtra
                    0.0,  // nocturnaExtra
                    0.0,  // dominical
                    0.0,  // festivo
                    locationTotalShifts,
                    employeeReports
            );
            locationReports.add(locationReport);

            globalTotalEmployees += shiftsByEmployee.size();
            globalTotalHours += locationTotalHours;
            globalTotalOvertime += locationTotalOvertime;
            globalTotalShifts += locationTotalShifts;
        }

        GlobalReportDto global = new GlobalReportDto(
                globalTotalEmployees,
                globalTotalHours,
                globalTotalOvertime,
                0.0,  // regularHours
                0.0,  // diurnaExtra
                0.0,  // nocturnaExtra
                0.0,  // dominical
                0.0,  // festivo
                globalTotalShifts
        );
        
        // Get all employees from filtered shifts
        List<EmployeeReportDto> allEmployees = shifts.stream()
                .map(shift -> shift.getEmployee().getId())
                .distinct()
                .map(empId -> {
                    List<Shift> empShifts = shifts.stream()
                            .filter(s -> s.getEmployee().getId().equals(empId))
                            .collect(Collectors.toList());
                    return calculateEmployeeReport(empShifts);
                })
                .filter(report -> report != null)
                .collect(Collectors.toList());

        return new ReportResponseDto(global, locationReports, allEmployees);
    }
}