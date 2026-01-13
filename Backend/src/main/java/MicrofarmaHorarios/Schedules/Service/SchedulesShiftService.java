package MicrofarmaHorarios.Schedules.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

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
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesBaseRepository;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftRepository;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftService;
import MicrofarmaHorarios.Notification.Service.EmailService;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;

@Service
public class SchedulesShiftService extends ASchedulesBaseService<Shift> implements ISchedulesShiftService {

    @Autowired
    private ISchedulesShiftRepository shiftRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ISecurityUserService userService;

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
        Shift savedShift = super.save(entity);
        try {
            emailService.sendShiftAssignmentEmail(savedShift);
        } catch (Exception e) {
            // Log error but don't fail the save operation
            System.err.println("Error sending shift assignment email: " + e.getMessage());
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

                // Time
                String time = convertTo12HourFormat(shift.getShiftType().getStartTime().toString()) + " - " +
                    convertTo12HourFormat(shift.getShiftType().getEndTime().toString());
                table.addCell(new Cell().add(new Paragraph(time).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setPadding(8));

                // Duration (calculate hours and minutes)
                String startTime = shift.getShiftType().getStartTime().toString();
                String endTime = shift.getShiftType().getEndTime().toString();
                String duration = calculateDuration(startTime, endTime);
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

    @Override
    public byte[] generateCalendarPdf(int year, int month, String locationId) throws Exception {
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            List<Shift> shifts = findByDateBetween(startDate, endDate);
            if (!locationId.isEmpty()) {
                shifts = shifts.stream().filter(s -> s.getLocation().getId().equals(locationId)).toList();
            }

            String locationName = locationId;
            if (!shifts.isEmpty()) {
                locationName = shifts.get(0).getLocation().getName();
            }

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

            // Header with pharmacy branding
            Table headerTable = new Table(new float[]{1});
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setMarginBottom(20);

            Cell headerCell = new Cell()
                .setBackgroundColor(emsRed)
                .setPadding(15);
            if (!locationId.isEmpty()) {
                headerCell.add(new Paragraph(locationName)
                    .setFontSize(24)
                    .setFontColor(white)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setMarginTop(5));
            } else {
                headerCell.add(new Paragraph("Sede")
                    .setFontSize(24)
                    .setFontColor(white)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setMarginTop(5));
            }
            headerCell.add(new Paragraph("Calendario de Turnos")
                .setFontSize(16)
                .setFontColor(white)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5));
            headerTable.addCell(headerCell);
            document.add(headerTable);

            // Month/Year title
            String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                              "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            Paragraph monthTitle = new Paragraph(months[month - 1] + " " + year)
                .setFontSize(20)
                .setFontColor(darkRed)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginBottom(15);
            document.add(monthTitle);

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
                        String time = shift.getShiftType().getStartTime().toString().substring(0, 5) + " - " +
                            shift.getShiftType().getEndTime().toString().substring(0, 5);

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
        return (List<Shift>) shiftRepository.saveAll(shifts);
    }

}