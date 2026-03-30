package MicrofarmaHorarios.Schedules.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
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
        if (entity.getEmployee() != null && entity.getEmployee().getUser() != null) {
            if (!"EMPLOYEE".equals(entity.getEmployee().getUser().getRole().getName())) {
                throw new Exception("Cannot assign shift to user without EMPLOYEE role");
            }
        }

        if (entity.getEmployee() != null && entity.getDate() != null) {
            List<Shift> existingShifts = shiftRepository.findByEmployeeId(entity.getEmployee().getId());
            List<Shift> shiftsForDate = existingShifts.stream()
                .filter(s -> s.getDate() != null && s.getDate().equals(entity.getDate()))
                .toList();

            if (!shiftsForDate.isEmpty()) {
                for (Shift existing : shiftsForDate) {
                    boolean isSameId = entity.getId() != null && entity.getId().equals(existing.getId());
                    boolean isNotDeleted = existing.getDeletedAt() == null;

                    if (!isSameId && isNotDeleted) {
                        String employeeName = entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName();
                        throw new Exception("El empleado " + employeeName + " ya tiene un turno asignado para la fecha " + entity.getDate());
                    }

                    if (isSameId || !isNotDeleted) {
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
            return String.format("%d:%02d%s", hour12, minute, period);
        } catch (Exception e) {
            return time24;
        }
    }

    private String formatTimeCompact(LocalTime time) {
        if (time == null) return "";
        int hour = time.getHour();
        int minute = time.getMinute();
        String period = hour >= 12 ? "pm" : "am";
        int hour12 = hour == 0 ? 12 : hour > 12 ? hour - 12 : hour;
        if (minute == 0) {
            return hour12 + period;
        } else {
            return hour12 + ":" + String.format("%02d", minute) + period;
        }
    }

    private String abbreviateShiftType(String typeName) {
        if (typeName == null) return "";
        String upper = typeName.toUpperCase();
        if (upper.contains("MAÑANA")) return "Maña";
        if (upper.contains("TARDE")) return "Tarde";
        if (upper.contains("NOCHE")) return "Noche";
        if (upper.contains("LARGO")) return "Largo";
        if (upper.contains("DESCANSO") || upper.contains("OFF")) return "Desc";
        return typeName;
    }

    // Formato: PrimerNombre Inicial. (ej: Teylor R.) - Primer nombre + inicial del primer apellido
    // El nombre completo debe tener: firstName = nombres, lastName = apellidos
    private String formatEmployeeName(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) return "";
        if (lastName == null || lastName.isEmpty()) return firstName;
        
        // Tomar el primer nombre (antes del segundo nombre si hay más de uno)
        String nombre = firstName.split(" ")[0];
        
        // Tomar el primer apellido (antes del segundo apellido si hay más de uno)
        String apellido = lastName.split(" ")[0];
        
        // La inicial del apellido
        String inicialApellido = apellido.substring(0, 1).toUpperCase();
        
        return nombre + " " + inicialApellido + ".";
    }

    @Override
    public byte[] generatePersonalShiftsPdf(String employeeId) throws Exception {
        // (este método no se modifica, pero lo incluyo igual)
        try {
            List<Shift> shifts = findByEmployeeId(employeeId);
            if (shifts.isEmpty()) {
                throw new Exception("No se encontraron turnos para este empleado");
            }

            String employeeName = shifts.get(0).getEmployee().getFirstName() + " " + shifts.get(0).getEmployee().getLastName();

            Color emsRed = new DeviceRgb(220, 20, 60);
            Color lightRed = new DeviceRgb(255, 235, 238);
            Color darkRed = new DeviceRgb(183, 28, 28);
            Color white = new DeviceRgb(255, 255, 255);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.LETTER);
            Document document = new Document(pdfDoc);
            document.setMargins(36, 36, 36, 36);

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

            shifts.sort((a, b) -> a.getDate().compareTo(b.getDate()));

            float[] columnWidths = {2, 2, 2, 2, 2, 2};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(10);

            String[] headers = {"Fecha", "Día", "Tipo de Turno", "Ubicación", "Horario", "Duración"};
            for (String header : headers) {
                Cell cell = new Cell()
                    .add(new Paragraph(header).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBold())
                    .setBackgroundColor(darkRed)
                    .setFontColor(white)
                    .setPadding(10);
                table.addCell(cell);
            }

            for (Shift shift : shifts) {
                String dateStr = shift.getDate().toString();
                String[] dateParts = dateStr.split("-");
                String formattedDate = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                table.addCell(new Cell().add(new Paragraph(formattedDate).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

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

                table.addCell(new Cell().add(new Paragraph(shift.getShiftType().getName()).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));
                table.addCell(new Cell().add(new Paragraph(shift.getLocation().getName()).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                String time;
                if (shift.getShiftType().getTimeRanges() != null && !shift.getShiftType().getTimeRanges().isEmpty()) {
                    time = shift.getShiftType().getFormattedTimeRanges();
                } else {
                    time = convertTo12HourFormat(shift.getShiftType().getStartTime().toString()) + " - " +
                           convertTo12HourFormat(shift.getShiftType().getEndTime().toString());
                }
                table.addCell(new Cell().add(new Paragraph(time).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                double durationHours;
                if (shift.getShiftType().getTimeRanges() != null && !shift.getShiftType().getTimeRanges().isEmpty()) {
                    durationHours = shift.getShiftType().getTotalDurationHours();
                } else {
                    durationHours = calculateDurationHours(shift.getShiftType().getStartTime(), shift.getShiftType().getEndTime());
                }
                String duration = String.format("%.1fh", durationHours);
                table.addCell(new Cell().add(new Paragraph(duration).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));
            }

            document.add(table);

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

    private double calculateDurationHours(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime == null || endTime == null) return 0.0;
        if (endTime.isBefore(startTime)) {
            int startToMidnight = 24 - startTime.getHour();
            int midnightToEnd = endTime.getHour();
            return startToMidnight + midnightToEnd;
        }
        return (endTime.getHour() - startTime.getHour()) + (endTime.getMinute() - startTime.getMinute()) / 60.0;
    }

    @Override
    public byte[] generateCalendarPdf(int year, int month, String locationId, String employeeId, boolean deliveryOnly) throws Exception {
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            List<Shift> shifts = findByDateBetween(startDate, endDate).stream()
                .filter(s -> s.getEmployee() != null)
                .toList();
            
            // Filter for delivery employees only if requested
            if (deliveryOnly) {
                shifts = shifts.stream()
                    .filter(s -> s.getEmployee().getPosition() != null && 
                                 s.getEmployee().getPosition().getName() != null &&
                                 s.getEmployee().getPosition().getName().toLowerCase().contains("domicili"))
                    .toList();
            }
            
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
                    headerTitle = "Empleado no encontrado";
                }
            }

            Color emsRed   = new DeviceRgb(220, 20, 60);
            Color lightRed = new DeviceRgb(255, 235, 238);
            Color lightGreen = new DeviceRgb(220, 255, 220);
            Color darkRed  = new DeviceRgb(183, 28, 28);
            Color white    = new DeviceRgb(255, 255, 255);
            Color lightGray = new DeviceRgb(243, 244, 246);

            String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                              "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.LETTER.rotate());

            Document document = new Document(pdfDoc);
            document.setMargins(28, 28, 28, 28);

            String mainTitle = "Calendario General";
            if (employeeId != null && !employeeId.isEmpty()) {
                mainTitle = "Empleado: " + headerTitle;
            } else if (locationId != null && !locationId.isEmpty()) {
                mainTitle = "Sede: " + locationName;
            }

            Paragraph title = new Paragraph(mainTitle)
                .setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(darkRed)
                .setBold()
                .setMarginBottom(6);
            document.add(title);

            Paragraph subtitle = new Paragraph("Calendario de Turnos - " + months[month - 1] + " " + year)
                .setFontSize(13)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(12);
            document.add(subtitle);

            Table headerTable = new Table(new float[]{1});
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setMarginBottom(14);

            Cell headerCell = new Cell().setBackgroundColor(emsRed).setPadding(8);
            headerCell.add(new Paragraph("Microfarma Horarios")
                .setFontSize(18)
                .setFontColor(white)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold());
            headerCell.add(new Paragraph("Sistema de Gestión de Horarios Laborales")
                .setFontSize(10)
                .setFontColor(white)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(3));
            headerTable.addCell(headerCell);
            document.add(headerTable);

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

            float[] columnWidths = {1, 1, 1, 1, 1, 1, 1};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(8);

            String[] daysOfWeek = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
            for (String day : daysOfWeek) {
                Cell cell = new Cell()
                    .add(new Paragraph(day).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setBold())
                    .setBackgroundColor(darkRed)
                    .setFontColor(white)
                    .setPadding(7);
                table.addHeaderCell(cell);
            }

            int firstDayOfWeek = startDate.getDayOfWeek().getValue() % 7;
            int daysInMonth = endDate.getDayOfMonth();

            for (int i = 0; i < firstDayOfWeek; i++) {
                Cell emptyCell = new Cell().setBackgroundColor(lightGray).setPadding(6);
                emptyCell.setKeepTogether(true);
                table.addCell(emptyCell);
            }

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = LocalDate.of(year, month, day);
                List<Shift> dayShifts = shifts.stream()
                    .filter(s -> s.getDate().equals(currentDate))
                    .sorted((a, b) -> a.getShiftType().getStartTime().compareTo(b.getShiftType().getStartTime()))
                    .toList();

                Cell cell = new Cell().setPadding(5).setBackgroundColor(white);
                cell.setKeepTogether(true);

                Paragraph dayNumber = new Paragraph(String.valueOf(day))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontColor(darkRed)
                    .setMarginBottom(2);
                cell.add(dayNumber);

                if (dayShifts.isEmpty()) {
                    Paragraph noShifts = new Paragraph("Sin turnos")
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY)
                        .setItalic();
                    cell.add(noShifts);
                } else {
                    for (Shift shift : dayShifts) {
                        // Usamos el nuevo método que da "Nombre, InicialApellido."
                        String employeeName = formatEmployeeName(
                            shift.getEmployee().getFirstName(),
                            shift.getEmployee().getLastName()
                        );
                        String shiftTypeName = shift.getShiftType().getName();
                        boolean isDescanso = shiftTypeName.toUpperCase().contains("DESCANSO")
                                          || shiftTypeName.toUpperCase().contains("OFF");

                        String shiftAbbr = abbreviateShiftType(shiftTypeName);

                        String time = "";
                        if (!isDescanso) {
                            if (shift.getShiftType().getTimeRanges() != null &&
                                !shift.getShiftType().getTimeRanges().isEmpty()) {
                                time = shift.getShiftType().getFormattedTimeRanges();
                            } else {
                                LocalTime start = shift.getShiftType().getStartTime();
                                LocalTime end = shift.getShiftType().getEndTime();
                                time = formatTimeCompact(start) + "-" + formatTimeCompact(end);
                            }
                        }

                        StringBuilder line = new StringBuilder();
                        line.append(employeeName);
                        line.append(" · ").append(shiftAbbr);
                        if (!isDescanso && !time.isEmpty()) {
                            line.append(" · ").append(time);
                        }

                        Color bgColor = isDescanso ? lightGreen : lightRed;
                        Paragraph shiftParagraph = new Paragraph(line.toString())
                            .setFontSize(6)
                            .setBackgroundColor(bgColor)
                            .setPadding(2)
                            .setMarginBottom(1)
                            .setMarginTop(0);
                        cell.add(shiftParagraph);
                    }
                }

                table.addCell(cell);
            }

            int totalCells = firstDayOfWeek + daysInMonth;
            int remainingCells = ((totalCells + 6) / 7 * 7) - totalCells;
            for (int i = 0; i < remainingCells; i++) {
                Cell emptyCell = new Cell().setBackgroundColor(lightGray).setPadding(6);
                emptyCell.setKeepTogether(true);
                table.addCell(emptyCell);
            }

            document.add(table);

            Paragraph footer = new Paragraph("Sistema de Gestión de Turnos - Microfarma | Generado el " +
                java.time.LocalDate.now().toString())
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(14);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
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
        List<Shift> shiftsToSave = new java.util.ArrayList<>();

        for (Shift shift : shifts) {
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
                var shiftTypeOpt = shiftTypeService.findById(shift.getShiftType().getId());
                if (shiftTypeOpt.isPresent()) {
                    shift.setShiftType(shiftTypeOpt.get());
                } else {
                    throw new Exception("Tipo de turno no encontrado con ID: " + shift.getShiftType().getId());
                }
            }

            if (shift.getEmployee() != null && shift.getDate() != null) {
                Optional<Shift> existingShift = shiftRepository.findByEmployeeAndDateAndStatusTrue(shift.getEmployee(), shift.getDate());
                if (existingShift.isPresent()) {
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