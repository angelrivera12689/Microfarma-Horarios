package MicrofarmaHorarios.Schedules.Controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IService.IOrganizationLocationService;
import MicrofarmaHorarios.Schedules.DTO.Response.EmployeeReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.OvertimeDetailDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;
import MicrofarmaHorarios.Schedules.IService.ISchedulesReportService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@RestController
@RequestMapping("/api/schedules/reports")
@PreAuthorize("hasAuthority('ADMIN')")
public class SchedulesReportController {

    @Autowired
    private ISchedulesReportService reportService;

    @Autowired
    private IOrganizationLocationService locationService;

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> generateMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            ReportResponseDto report = reportService.generateReport(month, year);
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte generado exitosamente", report, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al generar el reporte: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/monthly/by-location")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> generateMonthlyReportByLocation(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String locationId) {
        try {
            ReportResponseDto report = reportService.generateReportByLocation(month, year, locationId);
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte por ubicación generado exitosamente", report, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al generar el reporte por ubicación: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/monthly/csv")
    public ResponseEntity<byte[]> exportMonthlyReportCsv(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            ReportResponseDto report = reportService.generateReport(month, year);
            String csv = generateCsv(report);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "reporte_horas_" + year + "_" + month + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.getBytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> exportMonthlyReportPdf(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) String employeeId) {
        try {
            ReportResponseDto report;
            if (locationName != null && !locationName.isEmpty()) {
                // Find location by name to get id
                java.util.Optional<Location> locationOpt = locationService.findByName(locationName);
                if (locationOpt.isPresent()) {
                    String locationId = locationOpt.get().getId();
                    report = reportService.generateReportByLocation(month, year, locationId);
                } else {
                    throw new Exception("Location not found: " + locationName);
                }
            } else if (employeeId != null) {
                report = reportService.generateReport(month, year);
                // Filter employees
                report.setEmployees(report.getEmployees().stream()
                    .filter(emp -> emp.getEmployeeId().equals(employeeId))
                    .collect(java.util.stream.Collectors.toList()));
                // Also filter locations' employeeReports if needed, but for simplicity, keep.
            } else {
                report = reportService.generateReport(month, year);
            }
            byte[] pdf = generatePdf(report, month, year);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_horas_" + year + "_" + month + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String generateCsv(ReportResponseDto report) {
        StringBuilder csv = new StringBuilder();

        // Title and metadata
        csv.append("Microfarma Horarios - Reporte Mensual de Horas\n");
        csv.append("Generado el,").append(java.time.LocalDate.now()).append("\n\n");

        // Global summary
        if (report.getGlobal() != null) {
            csv.append("RESUMEN GLOBAL\n");
            csv.append("Total Empleados,").append(report.getGlobal().getTotalEmployees()).append("\n");
            csv.append("Total Horas,").append(String.format("%.2f", report.getGlobal().getTotalHours() != null ? report.getGlobal().getTotalHours() : 0.0)).append("\n");
            csv.append("Total Horas Extras,").append(String.format("%.2f", report.getGlobal().getTotalOvertimeHours() != null ? report.getGlobal().getTotalOvertimeHours() : 0.0)).append("\n");
            csv.append("Horas Regulares,").append(String.format("%.2f", report.getGlobal().getTotalRegularHours() != null ? report.getGlobal().getTotalRegularHours() : 0.0)).append("\n");
            csv.append("Extras Diurnas,").append(String.format("%.2f", report.getGlobal().getTotalDiurnaExtraHours() != null ? report.getGlobal().getTotalDiurnaExtraHours() : 0.0)).append("\n");
            csv.append("Extras Nocturnas,").append(String.format("%.2f", report.getGlobal().getTotalNocturnaExtraHours() != null ? report.getGlobal().getTotalNocturnaExtraHours() : 0.0)).append("\n");
            csv.append("Horas Dominicales,").append(String.format("%.2f", report.getGlobal().getTotalDominicalHours() != null ? report.getGlobal().getTotalDominicalHours() : 0.0)).append("\n");
            csv.append("Horas Festivas,").append(String.format("%.2f", report.getGlobal().getTotalFestivoHours() != null ? report.getGlobal().getTotalFestivoHours() : 0.0)).append("\n");
            csv.append("Total Turnos,").append(report.getGlobal().getTotalShifts() != null ? report.getGlobal().getTotalShifts() : 0).append("\n\n");
        }

        // Legend
        csv.append("GLOSARIO\n");
        csv.append("Horas Regulares,Horas trabajadas dentro del horario normal establecido\n");
        csv.append("Horas Extras Totales,Tiempo adicional trabajado fuera del horario regular\n");
        csv.append("Extras Diurnas,Horas extras trabajadas durante el día\n");
        csv.append("Extras Nocturnas,Horas extras trabajadas durante la noche\n");
        csv.append("Horas Dominicales,Horas trabajadas los domingos\n");
        csv.append("Horas Festivas,Horas trabajadas en días festivos\n");
        csv.append("Promedio Diario,Promedio de horas trabajadas por día\n");
        csv.append("Total Semanal,Total de horas en una semana típica\n\n");

        // Employee details
        csv.append("DETALLES POR EMPLEADO\n");
        csv.append("Nombre Completo,Horas Totales,Horas Regulares,Horas Extras Totales,Extras Diurnas,Extras Nocturnas,Horas Dominicales,Horas Festivas,Promedio Diario,Total Semanal,Total Turnos\n");

        for (EmployeeReportDto emp : report.getEmployees()) {
            csv.append(escapeCsv(emp.getFullName())).append(",")
               .append(String.format("%.2f", emp.getTotalHours())).append(",")
               .append(String.format("%.2f", emp.getRegularHours() != null ? emp.getRegularHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getOvertimeHours())).append(",")
               .append(String.format("%.2f", emp.getDiurnaExtraHours() != null ? emp.getDiurnaExtraHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getNocturnaExtraHours() != null ? emp.getNocturnaExtraHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getDominicalHours() != null ? emp.getDominicalHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getFestivoHours() != null ? emp.getFestivoHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getDailyAvgHours())).append(",")
               .append(String.format("%.2f", emp.getWeeklyTotalHours())).append(",")
               .append(emp.getTotalShifts() != null ? emp.getTotalShifts().toString() : "0").append("\n");
        }

        // Overtime details
        csv.append("\nDETALLES DE HORAS EXTRAS\n");
        for (EmployeeReportDto emp : report.getEmployees()) {
            if (emp.getOvertimeDetails() != null && !emp.getOvertimeDetails().isEmpty()) {
                csv.append("Empleado: ").append(escapeCsv(emp.getFullName())).append("\n");
                csv.append("Fecha,Horas,Justificación\n");
                for (OvertimeDetailDto ot : emp.getOvertimeDetails()) {
                    csv.append(ot.getDate() != null ? ot.getDate().toString() : "").append(",")
                       .append(String.format("%.2f", ot.getHours() != null ? ot.getHours() : 0.0)).append(",")
                       .append(escapeCsv(ot.getJustification() != null ? ot.getJustification() : "")).append("\n");
                }
                csv.append("\n");
            }
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private byte[] generatePdf(ReportResponseDto report, int month, int year) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(36, 36, 36, 36); // 1 inch margins

        try {
            // Header with branding
            Paragraph header = new Paragraph("Microfarma Horarios")
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5)
                    .setFontColor(ColorConstants.RED);
            document.add(header);

            // Subtitle
            Paragraph subtitle = new Paragraph("Sistema de Gestion de Horarios Laborales")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15)
                    .setFontColor(ColorConstants.GRAY);
            document.add(subtitle);

            // Title
            Paragraph title = new Paragraph("Reporte Mensual de Horas - " + month + "/" + year)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(title);

            // Legend with better formatting
            document.add(new Paragraph("Glosario de Terminos").setFontSize(14).setMarginBottom(10).setFontColor(ColorConstants.RED));
            document.add(new Paragraph("• Horas Regulares: Horas trabajadas dentro del horario normal establecido").setFontSize(10).setMarginBottom(3));
            document.add(new Paragraph("• Horas Extras Totales: Tiempo adicional trabajado fuera del horario regular").setFontSize(10).setMarginBottom(3));
            document.add(new Paragraph("• Extras Diurnas: Horas extras trabajadas de 6:00 AM a 7:00 PM").setFontSize(10).setMarginBottom(3));
            document.add(new Paragraph("• Extras Nocturnas: Horas extras trabajadas de 7:00 PM en adelante").setFontSize(10).setMarginBottom(3));
            document.add(new Paragraph("• Horas Dominicales: Horas trabajadas los domingos").setFontSize(10).setMarginBottom(3));
            document.add(new Paragraph("• Horas Festivas: Horas trabajadas en días festivos").setFontSize(10).setMarginBottom(3));
            document.add(new Paragraph("• Promedio Diario: Promedio de horas trabajadas por día").setFontSize(10).setMarginBottom(3));
            document.add(new Paragraph("• Total Semanal: Total de horas en una semana típica").setFontSize(10).setMarginBottom(15));

            // Global summary
            document.add(new Paragraph("Resumen Global del Mes").setFontSize(16).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15).setFontColor(ColorConstants.RED));

            // Create a professional summary table
            Table globalTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            globalTable.setWidth(UnitValue.createPercentValue(90));
            globalTable.setMarginBottom(20);

            // Light red header (company color)
            Color lightRed = new DeviceRgb(255, 235, 235); // Very light red
            globalTable.addHeaderCell(new Cell().add(new Paragraph("Concepto")).setBackgroundColor(lightRed).setFontSize(12).setTextAlignment(TextAlignment.CENTER));
            globalTable.addHeaderCell(new Cell().add(new Paragraph("Total")).setBackgroundColor(lightRed).setFontSize(12).setTextAlignment(TextAlignment.CENTER));

            // Light alternating colors
            Color lightGray = new DeviceRgb(245, 245, 245);

            // Data rows with alternating colors
            globalTable.addCell(new Cell().add(new Paragraph("Total Empleados")).setFontSize(11).setBackgroundColor(lightGray));
            globalTable.addCell(new Cell().add(new Paragraph(String.valueOf(report.getGlobal() != null ? report.getGlobal().getTotalEmployees() : 0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(lightGray));

            globalTable.addCell(new Cell().add(new Paragraph("Total Turnos")).setFontSize(11));
            globalTable.addCell(new Cell().add(new Paragraph(String.valueOf(report.getGlobal() != null && report.getGlobal().getTotalShifts() != null ? report.getGlobal().getTotalShifts() : 0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER));

            globalTable.addCell(new Cell().add(new Paragraph("Total Horas")).setFontSize(11).setBackgroundColor(lightGray));
            globalTable.addCell(new Cell().add(new Paragraph(String.format("%.2f h", report.getGlobal() != null && report.getGlobal().getTotalHours() != null ? report.getGlobal().getTotalHours() : 0.0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(lightGray));

            globalTable.addCell(new Cell().add(new Paragraph("Horas Regulares")).setFontSize(11));
            globalTable.addCell(new Cell().add(new Paragraph(String.format("%.2f h", report.getGlobal() != null && report.getGlobal().getTotalRegularHours() != null ? report.getGlobal().getTotalRegularHours() : 0.0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER));

            globalTable.addCell(new Cell().add(new Paragraph("Total Horas Extras")).setFontSize(11).setBackgroundColor(lightGray));
            globalTable.addCell(new Cell().add(new Paragraph(String.format("%.2f h", report.getGlobal() != null && report.getGlobal().getTotalOvertimeHours() != null ? report.getGlobal().getTotalOvertimeHours() : 0.0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(lightGray));

            globalTable.addCell(new Cell().add(new Paragraph("Extras Diurnas")).setFontSize(11));
            globalTable.addCell(new Cell().add(new Paragraph(String.format("%.2f h", report.getGlobal() != null && report.getGlobal().getTotalDiurnaExtraHours() != null ? report.getGlobal().getTotalDiurnaExtraHours() : 0.0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER));

            globalTable.addCell(new Cell().add(new Paragraph("Extras Nocturnas")).setFontSize(11).setBackgroundColor(lightGray));
            globalTable.addCell(new Cell().add(new Paragraph(String.format("%.2f h", report.getGlobal() != null && report.getGlobal().getTotalNocturnaExtraHours() != null ? report.getGlobal().getTotalNocturnaExtraHours() : 0.0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(lightGray));

            globalTable.addCell(new Cell().add(new Paragraph("Horas Dominicales")).setFontSize(11));
            globalTable.addCell(new Cell().add(new Paragraph(String.format("%.2f h", report.getGlobal() != null && report.getGlobal().getTotalDominicalHours() != null ? report.getGlobal().getTotalDominicalHours() : 0.0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER));

            globalTable.addCell(new Cell().add(new Paragraph("Horas Festivas")).setFontSize(11).setBackgroundColor(lightGray));
            globalTable.addCell(new Cell().add(new Paragraph(String.format("%.2f h", report.getGlobal() != null && report.getGlobal().getTotalFestivoHours() != null ? report.getGlobal().getTotalFestivoHours() : 0.0))).setFontSize(11).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(lightGray));

            document.add(globalTable);
            document.add(new Paragraph("").setMarginBottom(20));

            // Employee details
            document.add(new Paragraph("Detalles por Empleado").setFontSize(14).setMarginBottom(10).setFontColor(ColorConstants.RED));

            // Adjust column widths to fit better: narrower columns
            Table table = new Table(UnitValue.createPercentArray(new float[]{18, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setFontSize(9); // Smaller font for table

            // Light red headers (company color)
            table.addHeaderCell(new Cell().add(new Paragraph("Nombre")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Horas")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Regulares")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Extras Tot.")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Ext. Diurnas")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Ext. Noct.")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Dominicales")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Festivas")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Prom. Diario")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Tot. Semanal")).setBackgroundColor(lightRed).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Turnos")).setBackgroundColor(lightRed).setFontSize(9));

            if (report.getEmployees() != null) {
                boolean isAlternate = false;
                for (EmployeeReportDto emp : report.getEmployees()) {
                    Color rowColor = isAlternate ? lightGray : ColorConstants.WHITE;
                    table.addCell(new Cell().add(new Paragraph(emp != null && emp.getFullName() != null ? emp.getFullName() : "")).setFontSize(8).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getTotalHours() != null ? emp.getTotalHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getRegularHours() != null ? emp.getRegularHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getOvertimeHours() != null ? emp.getOvertimeHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getDiurnaExtraHours() != null ? emp.getDiurnaExtraHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getNocturnaExtraHours() != null ? emp.getNocturnaExtraHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getDominicalHours() != null ? emp.getDominicalHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getFestivoHours() != null ? emp.getFestivoHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getDailyAvgHours() != null ? emp.getDailyAvgHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.1f", emp != null && emp.getWeeklyTotalHours() != null ? emp.getWeeklyTotalHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    table.addCell(new Cell().add(new Paragraph(emp != null && emp.getTotalShifts() != null ? emp.getTotalShifts().toString() : "0")).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(rowColor));
                    isAlternate = !isAlternate;
                }
            }

            document.add(table);

            // Overtime details
            boolean hasOvertime = false;
            if (report.getEmployees() != null) {
                for (EmployeeReportDto emp : report.getEmployees()) {
                    if (emp != null && emp.getOvertimeDetails() != null && !emp.getOvertimeDetails().isEmpty()) {
                        hasOvertime = true;
                        break;
                    }
                }
            }

            if (hasOvertime) {
                document.add(new Paragraph("Detalles de Horas Extras").setFontSize(14).setMarginTop(30).setMarginBottom(10).setFontColor(ColorConstants.RED));

                if (report.getEmployees() != null) {
                    for (EmployeeReportDto emp : report.getEmployees()) {
                        if (emp != null && emp.getOvertimeDetails() != null && !emp.getOvertimeDetails().isEmpty()) {
                            // Employee header with summary
                            document.add(new Paragraph("Empleado: " + (emp.getFullName() != null ? emp.getFullName() : "")).setFontSize(11).setMarginTop(15).setMarginBottom(3));
                            document.add(new Paragraph("Total Extras: " + String.format("%.1f h", emp.getOvertimeHours()) +
                                                     " (Diurnas: " + String.format("%.1f h", emp.getDiurnaExtraHours() != null ? emp.getDiurnaExtraHours() : 0.0) +
                                                     ", Nocturnas: " + String.format("%.1f h", emp.getNocturnaExtraHours() != null ? emp.getNocturnaExtraHours() : 0.0) + ")")
                                       .setFontSize(9).setMarginBottom(5));

                            Table otTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
                            otTable.setWidth(UnitValue.createPercentValue(90));
                            otTable.setFontSize(8);
                            otTable.addHeaderCell(new Cell().add(new Paragraph("Fecha")).setBackgroundColor(lightRed).setFontSize(8));
                            otTable.addHeaderCell(new Cell().add(new Paragraph("Horas Extras")).setBackgroundColor(lightRed).setFontSize(8));
                            boolean otAlternate = false;
                            for (OvertimeDetailDto ot : emp.getOvertimeDetails()) {
                                Color otRowColor = otAlternate ? lightGray : ColorConstants.WHITE;
                                otTable.addCell(new Cell().add(new Paragraph(ot != null && ot.getDate() != null ? ot.getDate().toString() : "")).setFontSize(8).setBackgroundColor(otRowColor));
                                otTable.addCell(new Cell().add(new Paragraph(String.format("%.1f h", ot != null && ot.getHours() != null ? ot.getHours() : 0.0))).setFontSize(8).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.RED).setBackgroundColor(otRowColor));
                                otAlternate = !otAlternate;
                            }
                            document.add(otTable);

                            // Separator line
                            document.add(new Paragraph(" ").setMarginBottom(10));
                        }
                    }
                }
            }

            // Footer
            document.add(new Paragraph("Microfarma").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginTop(30).setFontColor(ColorConstants.RED));
            document.add(new Paragraph("Reporte generado automáticamente por el sistema Microfarma Horarios.").setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(5));
            document.add(new Paragraph("Fecha de generación: " + java.time.LocalDate.now()).setFontSize(9).setTextAlignment(TextAlignment.CENTER));

        } finally {
            document.flush();
            document.close();
        }

        return baos.toByteArray();
    }
}