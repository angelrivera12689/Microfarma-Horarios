package MicrofarmaHorarios.Schedules.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftService;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftTypeService;
import MicrofarmaHorarios.Notification.Service.EmailService;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.Organization.IService.IOrganizationLocationService;
import MicrofarmaHorarios.Organization.Entity.Location;

@Service
public class SchedulesShiftService extends ASchedulesBaseService<Shift> implements ISchedulesShiftService {

    @Autowired
    private ISchedulesShiftRepository shiftRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ISecurityUserService userService;

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    @Autowired
    private IOrganizationLocationService locationService;

    @Autowired
    private ISchedulesShiftTypeService shiftTypeService;

    @Override
    protected ISchedulesBaseRepository<Shift, String> getRepository() {
        return shiftRepository;
    }

    @Override
    public List<Shift> findByStateTrue() throws Exception {
        return super.findByStateTrue().stream()
                .filter(shift -> shift.getEmployee() != null)
                .toList();
    }

    @Override
    public Shift save(Shift entity) throws Exception {
        // Validate that the employee has EMPLOYEE role
        if (entity.getEmployee() != null && entity.getEmployee().getUser() != null) {
            if (!"EMPLOYEE".equals(entity.getEmployee().getUser().getRole().getName())) {
                throw new Exception("Cannot assign shift to user without EMPLOYEE role");
            }
        }
        
        // VALIDACIÓN PARA PREVENIR DUPLICADOS: Verificar si ya existe un turno para este empleado en esta fecha
        if (entity.getEmployee() != null && entity.getDate() != null) {
            // Buscar cualquier turno existente (sin importar status ni deleted_at)
            List<Shift> existingShifts = shiftRepository.findByEmployeeId(entity.getEmployee().getId());
            List<Shift> shiftsForDate = existingShifts.stream()
                .filter(s -> s.getDate() != null && s.getDate().equals(entity.getDate()))
                .toList();
            
            if (!shiftsForDate.isEmpty()) {
                // Verificar si hay un turno activo (no eliminado)
                // O si el turno existente es el mismo que se está actualizando
                for (Shift existing : shiftsForDate) {
                    boolean isSameId = entity.getId() != null && entity.getId().equals(existing.getId());
                    boolean isNotDeleted = existing.getDeletedAt() == null;
                    
                    // Si no es el mismo ID Y no está eliminado,不允许
                    if (!isSameId && isNotDeleted) {
                        String employeeName = entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName();
                        throw new Exception("El empleado " + employeeName + " ya tiene un turno asignado para la fecha " + entity.getDate());
                    }
                    
                    // Si es el mismo ID O está eliminado, permitir (reactivar)
                    if (isSameId || !isNotDeleted) {
                        // Reactivar turno eliminado o actualizar turno existente
                        existing.setDeletedAt(null);
                        existing.setStatus(true);
                        existing.setEmployee(entity.getEmployee());
                        existing.setLocation(entity.getLocation());
                        existing.setShiftType(entity.getShiftType());
                        existing.setDate(entity.getDate());
                        existing.setNotes(entity.getNotes());
                        existing.setUpdatedBy(entity.getCreatedBy());
                        return super.save(existing);
                    }
                }
            }
        }
        
        Shift savedShift = super.save(entity);
        try {
            emailService.sendShiftAssignmentEmail(savedShift);
        } catch (Exception e) {
            // Log error but don't fail the save operation
        }
        return savedShift;
    }

    @Override
    public List<Shift> findByEmployeeId(String employeeId) throws Exception {
        return shiftRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<Shift> findByUserId(String userId) throws Exception {
        var user = userService.findById(userId).orElseThrow(() -> new Exception("Usuario no encontrado"));
        if (user.getEmployee() == null) {
            throw new Exception("Usuario no tiene empleado asociado");
        }
        return shiftRepository.findByEmployeeId(user.getEmployee().getId());
    }

    @Override
    public List<Shift> findByLocationId(String locationId) throws Exception {
        return shiftRepository.findByLocationId(locationId);
    }

    @Override
    public List<Shift> findByDateBetween(LocalDate startDate, LocalDate endDate) throws Exception {
        return shiftRepository.findByDateBetween(startDate, endDate);
    }

    private String convertTo12HourFormat(String time24) {
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String period = hour >= 12 ? "PM" : "AM";
            int hour12 = hour == 0 ? 12 : hour > 12 ? hour - 12 : hour;
            return String.format("%d:%02d %s", hour12, minute, period);
        } catch (Exception e) {
            return time24; // fallback to original
        }
    }

    @Override
    public byte[] generatePersonalShiftsPdf(String employeeId) throws Exception {
        try {
            List<Shift> shifts = findByEmployeeId(employeeId);
            if (shifts.isEmpty()) {
                throw new Exception("No se encontraron turnos para este empleado");
            }

            String employeeName = shifts.get(0).getEmployee().getFirstName() + " " + shifts.get(0).getEmployee().getLastName();

            // Define EMS colors
            Color emsRed = new DeviceRgb(220, 20, 60); // Crimson red
            Color lightRed = new DeviceRgb(255, 235, 238); // Light red background
            Color darkRed = new DeviceRgb(183, 28, 28); // Dark red
            Color white = new DeviceRgb(255, 255, 255); // White
            Color lightGray = new DeviceRgb(243, 244, 246); // Light gray

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.LETTER);
            Document document = new Document(pdfDoc);
            document.setMargins(36, 36, 36, 36); // 0.5 inch margins

            // Header with employee name
            Table headerTable = new Table(new float[]{1});
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setMarginBottom(20);

            Cell headerCell = new Cell()
                .setBackgroundColor(emsRed)
                .setPadding(15);
            headerCell.add(new Paragraph("Mis Turnos")
                .setFontSize(24)
                .setFontColor(white)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginTop(5));
            headerCell.add(new Paragraph(employeeName)
                .setFontSize(16)
                .setFontColor(white)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5));
            headerTable.addCell(headerCell);
            document.add(headerTable);

            // Sort shifts by date
            shifts.sort((a, b) -> a.getDate().compareTo(b.getDate()));

            // Create shifts table
            float[] columnWidths = {2, 2, 2, 2, 2, 2}; // Date, Day, Shift Type, Location, Time, Duration
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(10);

            // Header row
            String[] headers = {"Fecha", "Día", "Tipo de Turno", "Ubicación", "Horario", "Duración"};
            for (String header : headers) {
                Cell cell = new Cell()
                    .add(new Paragraph(header).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBold())
                    .setBackgroundColor(darkRed)
                    .setFontColor(white)
                    .setPadding(10);
                table.addCell(cell);
            }

            // Data rows
            for (Shift shift : shifts) {
                // Date
                String dateStr = shift.getDate().toString();
                String[] dateParts = dateStr.split("-");
                String formattedDate = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                table.addCell(new Cell().add(new Paragraph(formattedDate).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                // Day of week
                String dayOfWeek = shift.getDate().getDayOfWeek().toString();
                String daySpanish = switch (dayOfWeek) {
                    case "MONDAY" -> "Lunes";
                    case "TUESDAY" -> "Martes";
                    case "WEDNESDAY" -> "Miércoles";
                    case "THURSDAY" -> "Jueves";
                    case "FRIDAY" -> "Viernes";
                    case "SATURDAY" -> "Sábado";
                    case "SUNDAY" -> "Domingo";
                    default -> dayOfWeek;
                };
                table.addCell(new Cell().add(new Paragraph(daySpanish).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                // Shift Type
                table.addCell(new Cell().add(new Paragraph(shift.getShiftType().getName()).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                // Location
                table.addCell(new Cell().add(new Paragraph(shift.getLocation().getName()).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                // Time - use formatted time ranges for multi-range shifts
                String time;
                if (shift.getShiftType().getTimeRanges() != null && 
                    !shift.getShiftType().getTimeRanges().isEmpty()) {
                    time = shift.getShiftType().getFormattedTimeRanges();
                } else {
                    time = convertTo12HourFormat(shift.getShiftType().getStartTime().toString()) + " - " +
                        convertTo12HourFormat(shift.getShiftType().getEndTime().toString());
                }
                table.addCell(new Cell().add(new Paragraph(time).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                // Duration (calculate hours and minutes) - use total duration for multi-range
                double durationHours;
                if (shift.getShiftType().getTimeRanges() != null && 
                    !shift.getShiftType().getTimeRanges().isEmpty()) {
                    durationHours = shift.getShiftType().getTotalDurationHours();
                } else {
                    durationHours = calculateDurationHours(shift.getShiftType().getStartTime(), 
                                                          shift.getShiftType().getEndTime());
                }
                String duration = String.format("%.1fh", durationHours);
                table.addCell(new Cell().add(new Paragraph(duration).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));
            }

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph("Sistema de Gestión de Turnos - Microfarma | Generado el " +
                java.time.LocalDate.now().toString())
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(20);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            // Fallback: create a simple PDF with error message
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.add(new Paragraph("Error generating PDF: " + e.getMessage()));
            document.close();
            return baos.toByteArray();
        }
    }

    private String calculateDuration(String startTime, String endTime) {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;

            // If end time is before start time, add 24 hours
            if (endTotalMinutes < startTotalMinutes) {
                endTotalMinutes += 24 * 60;
            }

            int durationMinutes = endTotalMinutes - startTotalMinutes;
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;

            return hours + "h " + minutes + "m";
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    /**
     * Calculates duration in hours from LocalTime objects.
     * Handles night shifts that cross midnight.
     * 
     * @param startTime Start time
     * @param endTime   End time
     * @return Duration in hours
     */
    private double calculateDurationHours(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return 0.0;
        }
        
        // Handle night shift crossing midnight
        if (endTime.isBefore(startTime)) {
            int startToMidnight = 24 - startTime.getHour();
            int midnightToEnd = endTime.getHour();
            return startToMidnight + midnightToEnd;
        }
        
        return (endTime.getHour() - startTime.getHour()) + 
               (endTime.getMinute() - startTime.getMinute()) / 60.0;
    }

    @Override
    public byte[] generateCalendarPdf(int year, int month, String locationId, String employeeId) throws Exception {
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            List<Shift> shifts = findByDateBetween(startDate, endDate).stream()
                .filter(s -> s.getEmployee() != null)
                .toList();
            if (locationId != null && !locationId.isEmpty()) {
                shifts = shifts.stream().filter(s -> s.getLocation().getId().equals(locationId)).toList();
            }
            if (employeeId != null && !employeeId.isEmpty()) {
                shifts = shifts.stream().filter(s -> s.getEmployee().getId().equals(employeeId)).toList();
            }

            String locationName = "";
            if (locationId != null && !locationId.isEmpty()) {
                try {
                    Optional<Location> locOpt = locationService.findById(locationId);
                    if (locOpt.isPresent()) {
                        locationName = locOpt.get().getName();
                    } else {
                        locationName = "Sede no encontrada";
                    }
                } catch (Exception e) {
                    locationName = "Sede no encontrada";
                }
            }
            String headerTitle = "";
            if (employeeId != null && !employeeId.isEmpty()) {
                try {
                    Optional<Employee> empOpt = employeeService.findById(employeeId);
                    if (empOpt.isPresent()) {
                        Employee emp = empOpt.get();
                        headerTitle = emp.getFirstName() + " " + emp.getLastName();
                    } else {
                        headerTitle = "Empleado no encontrado";
                    }
                } catch (Exception e) {
                    // Handle exception, perhaps log or set default
                    headerTitle = "Empleado no encontrado";
                }
            }

            // Define EMS colors
            Color emsRed = new DeviceRgb(220, 20, 60); // Crimson red
            Color lightRed = new DeviceRgb(255, 235, 238); // Light red background
            Color darkRed = new DeviceRgb(183, 28, 28); // Dark red
            Color white = new DeviceRgb(255, 255, 255); // White
            Color lightGray = new DeviceRgb(243, 244, 246); // Light gray

            String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                              "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.LETTER);
            Document document = new Document(pdfDoc);
            document.setMargins(36, 36, 36, 36); // 0.5 inch margins

            // Title
            String mainTitle = "Calendario General";
            if (employeeId != null && !employeeId.isEmpty()) {
                mainTitle = "Empleado: " + headerTitle;
            } else if (locationId != null && !locationId.isEmpty()) {
                mainTitle = "Sede: " + locationName;
            }
            Paragraph title = new Paragraph(mainTitle)
                .setFontSize(28)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(darkRed)
                .setBold()
                .setMarginBottom(10);
            document.add(title);

            // Subtitle
            Paragraph subtitle = new Paragraph("Calendario de Turnos - " + months[month - 1] + " " + year)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(20);
            document.add(subtitle);

            // Header with pharmacy branding
            Table headerTable = new Table(new float[]{1});
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setMarginBottom(20);

            Cell headerCell = new Cell()
                .setBackgroundColor(emsRed)
                .setPadding(10);
            headerCell.add(new Paragraph("Microfarma Horarios")
                .setFontSize(20)
                .setFontColor(white)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold());
            headerCell.add(new Paragraph("Sistema de Gestión de Horarios Laborales")
                .setFontSize(12)
                .setFontColor(white)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5));
            headerTable.addCell(headerCell);
            document.add(headerTable);

            // If no shifts, add a message
            if (shifts.isEmpty()) {
                String message = "No hay turnos programados";
                if (employeeId != null && !employeeId.isEmpty()) {
                    message += " para este empleado";
                } else if (locationId != null && !locationId.isEmpty()) {
                    message += " para esta sede";
                }
                message += " en el mes de " + months[month - 1] + " " + year + ".";
                Paragraph noShiftsMessage = new Paragraph(message)
                    .setFontSize(14)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setMarginTop(50)
                    .setMarginBottom(50);
                document.add(noShiftsMessage);
                document.close();
                return baos.toByteArray();
            }

            // Create calendar table
            float[] columnWidths = {1, 1, 1, 1, 1, 1, 1};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(10);

            // Header row with days of week
            String[] daysOfWeek = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
            for (String day : daysOfWeek) {
                Cell cell = new Cell()
                    .add(new Paragraph(day).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBold())
                    .setBackgroundColor(darkRed)
                    .setFontColor(white)
                    .setPadding(10);
                table.addCell(cell);
            }

            // Calculate first day of month
            int firstDayOfWeek = startDate.getDayOfWeek().getValue() % 7; // 0 = Sunday
            int daysInMonth = endDate.getDayOfMonth();

            // Empty cells before first day
            for (int i = 0; i < firstDayOfWeek; i++) {
                table.addCell(new Cell().setBackgroundColor(lightGray).setPadding(8));
            }

            // Days of the month
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = LocalDate.of(year, month, day);
                List<Shift> dayShifts = shifts.stream()
                    .filter(s -> s.getDate().equals(currentDate))
                    .sorted((a, b) -> a.getShiftType().getStartTime().compareTo(b.getShiftType().getStartTime()))
                    .toList();

                Cell cell = new Cell().setPadding(8).setBackgroundColor(white);

                // Day number
                Paragraph dayNumber = new Paragraph(String.valueOf(day))
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontColor(darkRed)
                    .setMarginBottom(5);
                cell.add(dayNumber);

                if (dayShifts.isEmpty()) {
                    Paragraph noShifts = new Paragraph("Sin turnos")
                        .setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY)
                        .setItalic();
                    cell.add(noShifts);
                } else {
                    for (Shift shift : dayShifts) {
                        String employeeName = shift.getEmployee().getFirstName() + " " + shift.getEmployee().getLastName();
                        String shiftType = shift.getShiftType().getName();
                        
                        // Use formatted time ranges for multi-range shifts
                        String time;
                        if (shift.getShiftType().getTimeRanges() != null && 
                            !shift.getShiftType().getTimeRanges().isEmpty()) {
                            time = shift.getShiftType().getFormattedTimeRanges();
                        } else {
                            time = shift.getShiftType().getStartTime().toString().substring(0, 5) + " - " +
                                shift.getShiftType().getEndTime().toString().substring(0, 5);
                        }

                        // Shift box with light red background
                        Table shiftTable = new Table(new float[]{1});
                        Cell shiftCell = new Cell()
                            .setBackgroundColor(lightRed)
                            .setPadding(4)
                            .setMarginBottom(3);

                        shiftCell.add(new Paragraph("👤 " + employeeName)
                            .setFontSize(8)
                            .setBold()
                            .setFontColor(darkRed));
                        shiftCell.add(new Paragraph("⏰ " + shiftType)
                            .setFontSize(7)
                            .setFontColor(ColorConstants.BLACK));
                        shiftCell.add(new Paragraph("🕐 " + convertTo12HourFormat(shift.getShiftType().getStartTime().toString()) + " - " +
                            convertTo12HourFormat(shift.getShiftType().getEndTime().toString()))
                            .setFontSize(7)
                            .setFontColor(ColorConstants.DARK_GRAY));

                        shiftTable.addCell(shiftCell);
                        cell.add(shiftTable);
                    }
                }

                table.addCell(cell);
            }

            // Fill remaining cells
            int totalCells = firstDayOfWeek + daysInMonth;
            int remainingCells = ((totalCells + 6) / 7 * 7) - totalCells;
            for (int i = 0; i < remainingCells; i++) {
                table.addCell(new Cell().setBackgroundColor(lightGray).setPadding(8));
            }

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph("Sistema de Gestión de Turnos - Microfarma | Generado el " +
                java.time.LocalDate.now().toString())
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(20);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            // Fallback: create a simple PDF with error message
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.add(new Paragraph("Error generating PDF: " + e.getMessage()));
            document.close();
            return baos.toByteArray();
        }
    }

    @Override
    public List<Shift> saveAll(List<Shift> shifts) throws Exception {
        // VALIDACIÓN PARA PREVENIR DUPLICADOS: Verificar cada turno antes de guardar
        List<Shift> shiftsToSave = new java.util.ArrayList<>();
        
        for (Shift shift : shifts) {
            // Cargar las entidades completas desde la BD usando los IDs proporcionados
            if (shift.getEmployee() != null && shift.getEmployee().getId() != null) {
                var employeeOpt = employeeService.findById(shift.getEmployee().getId());
                if (employeeOpt.isPresent()) {
                    shift.setEmployee(employeeOpt.get());
                } else {
                    throw new Exception("Empleado no encontrado con ID: " + shift.getEmployee().getId());
                }
            }
            
            if (shift.getLocation() != null && shift.getLocation().getId() != null) {
                var locationOpt = locationService.findById(shift.getLocation().getId());
                if (locationOpt.isPresent()) {
                    shift.setLocation(locationOpt.get());
                } else {
                    throw new Exception("Ubicación no encontrada con ID: " + shift.getLocation().getId());
                }
            }
            
            if (shift.getShiftType() != null && shift.getShiftType().getId() != null) {
                // Cargar el shiftType completo
                var shiftTypeOpt = shiftTypeService.findById(shift.getShiftType().getId());
                if (shiftTypeOpt.isPresent()) {
                    shift.setShiftType(shiftTypeOpt.get());
                } else {
                    throw new Exception("Tipo de turno no encontrado con ID: " + shift.getShiftType().getId());
                }
            }
            
            if (shift.getEmployee() != null && shift.getDate() != null) {
                // Solo buscar turnos ACTIVOS (status = true) para permitir crear nuevos turnos
                // aunque existan turnos eliminados (soft deleted)
                Optional<Shift> existingShift = shiftRepository.findByEmployeeAndDateAndStatusTrue(shift.getEmployee(), shift.getDate());
                if (existingShift.isPresent()) {
                    // Si es una actualización (mismo ID), permitir; si es nuevo, omitir
                    if (shift.getId() == null || !shift.getId().equals(existingShift.get().getId())) {
                        String employeeName = shift.getEmployee().getFirstName() + " " + shift.getEmployee().getLastName();
                        throw new Exception("El empleado " + employeeName + " ya tiene un turno asignado para la fecha " + shift.getDate() + ". Los turnos duplicados no serán guardados.");
                    }
                }
            }
            shiftsToSave.add(shift);
        }
        
        return (List<Shift>) shiftRepository.saveAll(shiftsToSave);
    }

}