package MicrofarmaHorarios.Schedules.Controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.kernel.colors.ColorConstants;
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
            @RequestParam int year) {
        try {
            ReportResponseDto report = reportService.generateReport(month, year);
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
        csv.append("Nombre Completo,Horas Diarias Promedio,Horas Semanales Totales,Horas Totales,Horas Extras,Horas Regulares,Horas Extras Diurnas,Horas Extras Nocturnas,Horas Dominicales,Horas Festivas\n");
        for (EmployeeReportDto emp : report.getEmployees()) {
            csv.append(escapeCsv(emp.getFullName())).append(",")
               .append(String.format("%.2f", emp.getDailyAvgHours())).append(",")
               .append(String.format("%.2f", emp.getWeeklyTotalHours())).append(",")
               .append(String.format("%.2f", emp.getTotalHours())).append(",")
               .append(String.format("%.2f", emp.getOvertimeHours())).append(",")
               .append(String.format("%.2f", emp.getRegularHours() != null ? emp.getRegularHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getDiurnaExtraHours() != null ? emp.getDiurnaExtraHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getNocturnaExtraHours() != null ? emp.getNocturnaExtraHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getDominicalHours() != null ? emp.getDominicalHours() : 0)).append(",")
               .append(String.format("%.2f", emp.getFestivoHours() != null ? emp.getFestivoHours() : 0)).append("\n");
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
            // Header
            Paragraph header = new Paragraph("Microfarma Horarios")
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(header);

            // Title
            Paragraph title = new Paragraph("Reporte Mensual de Horas - " + month + "/" + year)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Global summary
            document.add(new Paragraph("Resumen Global").setFontSize(14).setMarginBottom(10));
            document.add(new Paragraph("Total Empleados: " + (report.getGlobal() != null ? report.getGlobal().getTotalEmployees() : 0)).setMarginBottom(5));
            document.add(new Paragraph("Total Horas: " + String.format("%.2f", report.getGlobal() != null && report.getGlobal().getTotalHours() != null ? report.getGlobal().getTotalHours() : 0.0)).setMarginBottom(5));
            document.add(new Paragraph("Total Horas Extras: " + String.format("%.2f", report.getGlobal() != null && report.getGlobal().getTotalOvertimeHours() != null ? report.getGlobal().getTotalOvertimeHours() : 0.0)).setMarginBottom(5));
            document.add(new Paragraph("Horas Regulares: " + String.format("%.2f", report.getGlobal() != null && report.getGlobal().getTotalRegularHours() != null ? report.getGlobal().getTotalRegularHours() : 0.0)).setMarginBottom(5));
            document.add(new Paragraph("Horas Extras Diurnas: " + String.format("%.2f", report.getGlobal() != null && report.getGlobal().getTotalDiurnaExtraHours() != null ? report.getGlobal().getTotalDiurnaExtraHours() : 0.0)).setMarginBottom(5));
            document.add(new Paragraph("Horas Extras Nocturnas: " + String.format("%.2f", report.getGlobal() != null && report.getGlobal().getTotalNocturnaExtraHours() != null ? report.getGlobal().getTotalNocturnaExtraHours() : 0.0)).setMarginBottom(5));
            document.add(new Paragraph("Horas Dominicales: " + String.format("%.2f", report.getGlobal() != null && report.getGlobal().getTotalDominicalHours() != null ? report.getGlobal().getTotalDominicalHours() : 0.0)).setMarginBottom(5));
            document.add(new Paragraph("Horas Festivas: " + String.format("%.2f", report.getGlobal() != null && report.getGlobal().getTotalFestivoHours() != null ? report.getGlobal().getTotalFestivoHours() : 0.0)).setMarginBottom(20));

            // Employee details
            document.add(new Paragraph("Detalles por Empleado").setFontSize(14).setMarginBottom(10));

            Table table = new Table(UnitValue.createPercentArray(new float[]{18, 8, 8, 8, 8, 8, 8, 8, 8, 8}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header cells
            table.addHeaderCell(new Cell().add(new Paragraph("Nombre")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Diarias Prom")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Semanales")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Totales")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Extras")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Regulares")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Ext Diurnas")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Ext Nocturnas")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Dominicales")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Festivas")).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            if (report.getEmployees() != null) {
                for (EmployeeReportDto emp : report.getEmployees()) {
                    table.addCell(new Cell().add(new Paragraph(emp != null && emp.getFullName() != null ? emp.getFullName() : "")));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getDailyAvgHours() != null ? emp.getDailyAvgHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getWeeklyTotalHours() != null ? emp.getWeeklyTotalHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getTotalHours() != null ? emp.getTotalHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getOvertimeHours() != null ? emp.getOvertimeHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getRegularHours() != null ? emp.getRegularHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getDiurnaExtraHours() != null ? emp.getDiurnaExtraHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getNocturnaExtraHours() != null ? emp.getNocturnaExtraHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getDominicalHours() != null ? emp.getDominicalHours() : 0.0))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", emp != null && emp.getFestivoHours() != null ? emp.getFestivoHours() : 0.0))));
                }
            }

            document.add(table);

            // Overtime details
            if (report.getEmployees() != null) {
                for (EmployeeReportDto emp : report.getEmployees()) {
                    if (emp != null && emp.getOvertimeDetails() != null && !emp.getOvertimeDetails().isEmpty()) {
                        document.add(new Paragraph("\nHoras Extras - " + (emp.getFullName() != null ? emp.getFullName() : "")).setFontSize(12).setMarginTop(20).setMarginBottom(10));
                        Table otTable = new Table(UnitValue.createPercentArray(new float[]{30, 15, 55}));
                        otTable.setWidth(UnitValue.createPercentValue(100));
                        otTable.addHeaderCell(new Cell().add(new Paragraph("Fecha")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                        otTable.addHeaderCell(new Cell().add(new Paragraph("Horas")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                        otTable.addHeaderCell(new Cell().add(new Paragraph("Justificación")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                        for (OvertimeDetailDto ot : emp.getOvertimeDetails()) {
                            otTable.addCell(new Cell().add(new Paragraph(ot != null && ot.getDate() != null ? ot.getDate().toString() : "")));
                            otTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", ot != null && ot.getHours() != null ? ot.getHours() : 0.0))));
                            otTable.addCell(new Cell().add(new Paragraph(ot != null && ot.getJustification() != null ? ot.getJustification() : "")));
                        }
                        document.add(otTable);
                    }
                }
            }

            // Simple message
            document.add(new Paragraph("Reporte generado exitosamente.").setFontSize(12));

            // Footer
            document.add(new Paragraph("\nGenerado el " + java.time.LocalDate.now()).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginTop(30));

        } finally {
            document.flush();
            document.close();
        }

        return baos.toByteArray();
    }
}