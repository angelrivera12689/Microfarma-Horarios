package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MicrofarmaHorarios.Schedules.DTO.Response.EmployeeReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.GlobalReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.OvertimeDetailDto;
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

        Map<String, List<Shift>> shiftsByEmployee = shifts.stream()
                .filter(shift -> shift.getEmployee().getId() != null)
                .collect(Collectors.groupingBy(shift -> shift.getEmployee().getId()));

        List<EmployeeReportDto> employeeReports = new ArrayList<>();
        double globalTotalHours = 0;
        double globalTotalOvertime = 0;
        double globalRegularHours = 0;
        double globalDiurnaExtra = 0;
        double globalNocturnaExtra = 0;
        double globalDominical = 0;
        double globalFestivo = 0;

        for (Map.Entry<String, List<Shift>> entry : shiftsByEmployee.entrySet()) {
            List<Shift> employeeShifts = entry.getValue();
            EmployeeReportDto report = calculateEmployeeReport(employeeShifts);

            if (report != null) {
                employeeReports.add(report);
                globalTotalHours += report.getTotalHours();
                globalTotalOvertime += report.getOvertimeHours();
                globalRegularHours += report.getRegularHours() != null ? report.getRegularHours() : 0;
                globalDiurnaExtra += report.getDiurnaExtraHours() != null ? report.getDiurnaExtraHours() : 0;
                globalNocturnaExtra += report.getNocturnaExtraHours() != null ? report.getNocturnaExtraHours() : 0;
                globalDominical += report.getDominicalHours() != null ? report.getDominicalHours() : 0;
                globalFestivo += report.getFestivoHours() != null ? report.getFestivoHours() : 0;
            }
        }

        GlobalReportDto global = new GlobalReportDto(
                shiftsByEmployee.size(),
                globalTotalHours,
                globalTotalOvertime,
                globalRegularHours,
                globalDiurnaExtra,
                globalNocturnaExtra,
                globalDominical,
                globalFestivo
        );
        return new ReportResponseDto(global, employeeReports);
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

            // Debug logging
            System.out.println("Shift date: " + shift.getDate() + ", isSunday: " + isSunday + ", isHoliday: " + isHoliday + ", hours: " + hours);

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
                overtimeDetails.add(new OvertimeDetailDto(shift.getDate(), extraForThisShift, shift.getNotes()));

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

        String firstName = shifts.get(0).getEmployee().getFirstName() != null ? shifts.get(0).getEmployee().getFirstName() : "";
        String lastName = shifts.get(0).getEmployee().getLastName() != null ? shifts.get(0).getEmployee().getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();

        return new EmployeeReportDto(
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
                festivoHours
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
}