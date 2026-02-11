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

        // Collect all employees from all locations
        List<EmployeeReportDto> allEmployees = locationReports.stream()
                .flatMap(location -> location.getEmployeeReports().stream())
                .collect(Collectors.toList());

        GlobalReportDto global = new GlobalReportDto(
                globalTotalEmployees,
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

        for (Shift shift : shifts) {
            if (shift.getShiftType() == null) continue;
            double hours = calculateHours(shift.getShiftType().getStartTime(), shift.getShiftType().getEndTime());
            totalHours += hours;

            // Determinar tipo de día
            boolean isSunday = shift.getDate().getDayOfWeek().getValue() == 7;
            boolean isHoliday = holidayService.isHoliday(shift.getDate());
            boolean isSpecialDay = isSunday || isHoliday;

            // Debug logging removed

            double regularHoursPerDay = 8.0;
            double regularForThisShift = Math.min(hours, regularHoursPerDay);
            double extraForThisShift = Math.max(0, hours - regularHoursPerDay);

            // Horas regulares
            if (isSpecialDay) {
                if (isSunday) {
                    dominicalHours += regularForThisShift;
                } else {
                    festivoHours += regularForThisShift;
                }
            } else {
                regularHours += regularForThisShift;
            }

            // Horas extras
            if (extraForThisShift > 0) {
                overtimeHours += extraForThisShift;
                overtimeDetails.add(new OvertimeDetailDto(shift.getDate(), extraForThisShift, shift.getNotes(), shift.getLocation().getName()));

                // Categorizar horas extras por tiempo
                double diurnaExtra = calculateDiurnaExtraHours(shift.getShiftType().getStartTime(), shift.getShiftType().getEndTime(), regularHoursPerDay);
                double nocturnaExtra = extraForThisShift - diurnaExtra;

                diurnaExtraHours += diurnaExtra;
                nocturnaExtraHours += nocturnaExtra;

                // Las horas extras en días especiales también se cuentan en dominical/festivo
                if (isSpecialDay) {
                    if (isSunday) {
                        dominicalHours += extraForThisShift;
                    } else {
                        festivoHours += extraForThisShift;
                    }
                }
            }
        }

        double dailyAvg = workingDays > 0 ? totalHours / workingDays : 0;
        double weeklyTotal = totalHours / weeksInMonth;

        String employeeId = shifts.get(0).getEmployee().getId();
        String firstName = shifts.get(0).getEmployee().getFirstName() != null ? shifts.get(0).getEmployee().getFirstName() : "";
        String lastName = shifts.get(0).getEmployee().getLastName() != null ? shifts.get(0).getEmployee().getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();

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
                workingDays
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
     */
    private double calculateDiurnaExtraHours(LocalTime start, LocalTime end, double regularHours) {
        if (start == null || end == null || regularHours <= 0) {
            return 0.0;
        }

        LocalTime diurnaStart = LocalTime.of(6, 0);
        LocalTime diurnaEnd = LocalTime.of(19, 0); // 7:00 PM

        // Calcular horas totales del turno
        double totalHours = calculateHours(start, end);
        double extraHours = Math.max(0, totalHours - regularHours);

        if (extraHours == 0) {
            return 0.0;
        }

        // Para simplificar, asumimos que las horas extras siguen el patrón del turno
        // Si el turno termina después de 7 PM, las extras son nocturnas
        // Si termina antes o igual a 7 PM, son diurnas
        if (end.isAfter(diurnaEnd) || (end.equals(diurnaEnd) && totalHours > regularHours)) {
            // Verificar si las horas extras ocurren en horario nocturno
            LocalTime extraStart = start.plusHours((long) regularHours);
            if (extraStart.isBefore(diurnaEnd)) {
                // Parte diurna de las extras
                double diurnaPortion = Math.min(extraHours, calculateHours(extraStart, diurnaEnd));
                return diurnaPortion;
            }
        }

        // Si todo el turno está en horario diurno, todas las extras son diurnas
        return extraHours;
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
                .filter(shift -> shift.getEmployee() != null && shift.getEmployee().getFirstName() != null)
                .map(shift -> new ReportFiltersDto.EmployeeFilterOption(
                        shift.getEmployee().getId(),
                        shift.getEmployee().getFirstName() + " " + shift.getEmployee().getLastName(),
                        shift.getEmployee().getEmail(),
                        shift.getEmployee().getPosition() != null ? shift.getEmployee().getPosition().getName() : null,
                        shift.getLocation() != null ? shift.getLocation().getId() : null))
                .distinct()
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
}