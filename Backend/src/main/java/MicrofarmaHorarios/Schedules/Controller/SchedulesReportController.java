package MicrofarmaHorarios.Schedules.Controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IService.IOrganizationLocationService;
import MicrofarmaHorarios.Schedules.DTO.Response.EmployeeReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.GlobalReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.LocationReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.OvertimeDetailDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportFiltersDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;
import MicrofarmaHorarios.Schedules.IService.ISchedulesReportService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@RestController
@RequestMapping("/api/schedules/reports")
// Temporarily allow all authenticated users for testing
// @PreAuthorize("hasAuthority('ADMIN')")
public class SchedulesReportController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulesReportController.class);

    @Autowired
    private ISchedulesReportService reportService;

    @Autowired
    private IOrganizationLocationService locationService;

    // ==================== FILTER ENDPOINTS ====================

    @GetMapping("/filters")
    public ResponseEntity<ApiResponseDto<ReportFiltersDto>> getAvailableFilters() {
        try {
            ReportFiltersDto filters = reportService.getAvailableFilters();
            return ResponseEntity.ok(new ApiResponseDto<>("Filtros obtenidos exitosamente", filters, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al obtener filtros: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/filters/locations")
    public ResponseEntity<ApiResponseDto<List<ReportFiltersDto.LocationFilterOption>>> getLocationFilters() {
        try {
            ReportFiltersDto filters = reportService.getAvailableFilters();
            return ResponseEntity.ok(new ApiResponseDto<>("Filtros de sedes obtenidos", filters.getLocations(), true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al obtener sedes: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/filters/employees")
    public ResponseEntity<ApiResponseDto<List<ReportFiltersDto.EmployeeFilterOption>>> getEmployeeFilters() {
        try {
            ReportFiltersDto filters = reportService.getAvailableFilters();
            return ResponseEntity.ok(new ApiResponseDto<>("Filtros de empleados obtenidos", filters.getEmployees(), true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al obtener empleados: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/filters/years")
    public ResponseEntity<ApiResponseDto<List<ReportFiltersDto.YearOption>>> getYearFilters() {
        try {
            ReportFiltersDto filters = reportService.getAvailableFilters();
            return ResponseEntity.ok(new ApiResponseDto<>("Filtros de años obtenidos", filters.getYears(), true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al obtener años: " + e.getMessage(), null, false));
        }
    }

    // ==================== REPORT ENDPOINTS ====================

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> generateMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            ReportResponseDto report = reportService.generateReport(month, year);
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte general generado exitosamente", report, true));
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
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte por sede generado exitosamente", report, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al generar reporte por sede: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/monthly/by-employee")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> generateMonthlyReportByEmployee(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String employeeId) {
        try {
            ReportResponseDto report = reportService.generateReportByEmployee(month, year, employeeId);
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte por empleado generado exitosamente", report, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al generar reporte por empleado: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/global")
    public ResponseEntity<ApiResponseDto<GlobalReportDto>> generateGlobalReport(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            GlobalReportDto report = reportService.generateGlobalReport(month, year);
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte global generado exitosamente", report, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al generar reporte global: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponseDto<List<LocationReportDto>>> generateLocationReport(
            @PathVariable String locationId,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            List<LocationReportDto> report = reportService.generateLocationReport(month, year, locationId);
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte de sede generado exitosamente", report, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al generar reporte de sede: " + e.getMessage(), null, false));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<EmployeeReportDto>> generateEmployeeIndividualReport(
            @PathVariable String employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            EmployeeReportDto report = reportService.generateEmployeeIndividualReport(month, year, employeeId);
            return ResponseEntity.ok(new ApiResponseDto<>("Reporte de empleado generado exitosamente", report, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>("Error al generar reporte de empleado: " + e.getMessage(), null, false));
        }
    }

    // ==================== PDF EXPORT ENDPOINTS ====================

    @GetMapping("/monthly/pdf/general")
    public ResponseEntity<byte[]> exportGeneralReportPdf(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            ReportResponseDto report = reportService.generateReport(month, year);
            byte[] pdf = generateGeneralPdf(report, month, year, "general");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_general_" + year + "_" + month + ".pdf");

            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/monthly/pdf/location")
    public ResponseEntity<byte[]> exportLocationReportPdf(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String locationId) {
        try {
            ReportResponseDto report = reportService.generateReportByLocation(month, year, locationId);
            Location location = locationService.findById(locationId).orElse(null);
            String locationName = location != null ? location.getName() : "Sede";
            byte[] pdf = generateLocationPdf(report, month, year, locationName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_sede_" + year + "_" + month + ".pdf");

            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/monthly/pdf/employee")
    public ResponseEntity<byte[]> exportEmployeeReportPdf(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String employeeId) {
        try {
            ReportResponseDto report = reportService.generateReportByEmployee(month, year, employeeId);
            EmployeeReportDto employee = report.getEmployees() != null && !report.getEmployees().isEmpty() 
                    ? report.getEmployees().get(0) : null;
            String employeeName = employee != null ? employee.getFullName() : "Empleado";
            byte[] pdf = generateEmployeePdf(report, month, year, employeeName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_empleado_" + year + "_" + month + ".pdf");

            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> exportMonthlyReportPdf(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false, defaultValue = "general") String reportType) {
        try {
            ReportResponseDto report;
            String filename;
            
            switch (reportType.toLowerCase()) {
                case "location":
                    if (locationName != null && !locationName.isEmpty()) {
                        java.util.Optional<Location> locationOpt = locationService.findByName(locationName);
                        if (locationOpt.isPresent()) {
                            report = reportService.generateReportByLocation(month, year, locationOpt.get().getId());
                            return exportLocationReportPdf(month, year, locationOpt.get().getId());
                        }
                    }
                    report = reportService.generateReport(month, year);
                    filename = "reporte_" + year + "_" + month + ".pdf";
                    break;
                case "employee":
                    if (employeeId != null) {
                        return exportEmployeeReportPdf(month, year, employeeId);
                    }
                    report = reportService.generateReport(month, year);
                    filename = "reporte_" + year + "_" + month + ".pdf";
                    break;
                default:
                    report = reportService.generateReport(month, year);
                    filename = "reporte_general_" + year + "_" + month + ".pdf";
            }
            
            byte[] pdf = generateGeneralPdf(report, month, year, reportType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/monthly/csv")
    public ResponseEntity<byte[]> exportMonthlyReportCsv(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            ReportResponseDto report = reportService.generateReport(month, year);
            String csv = generateCsv(report, month, year);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "reporte_horas_" + year + "_" + month + ".csv");

            return ResponseEntity.ok().headers(headers).body(csv.getBytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== PDF GENERATION METHODS ====================

    private byte[] generateGeneralPdf(ReportResponseDto report, int month, int year, String reportType) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4.rotate());
        document.setMargins(30, 30, 30, 30);

        try {
            Color lightRed = new DeviceRgb(255, 235, 235);
            Color lightGray = new DeviceRgb(245, 245, 245);
            
            // Header
            addDocumentHeader(document, "REPORTE GENERAL DE HORAS", month, year);

            // Global summary
            document.add(new Paragraph("Resumen Global").setFontSize(14).setFontColor(ColorConstants.RED).setMarginTop(10));
            Table globalTable = new Table(UnitValue.createPercentArray(new float[]{30, 20, 20, 20}));
            globalTable.setWidth(UnitValue.createPercentValue(100));
            
            globalTable.addHeaderCell(createHeaderCell("Concepto", lightRed));
            globalTable.addHeaderCell(createHeaderCell("Total", lightRed));
            globalTable.addHeaderCell(createHeaderCell("Concepto", lightRed));
            globalTable.addHeaderCell(createHeaderCell("Total", lightRed));

            addSummaryRow(globalTable, "Total Empleados", report.getGlobal().getTotalEmployees(), lightGray);
            addSummaryRow(globalTable, "Total Turnos", report.getGlobal().getTotalShifts(), null);
            addSummaryRow(globalTable, "Horas Totales", String.format("%.2f h", report.getGlobal().getTotalHours()), lightGray);
            addSummaryRow(globalTable, "Horas Extras", String.format("%.2f h", report.getGlobal().getTotalOvertimeHours()), null);
            addSummaryRow(globalTable, "Horas Regulares", String.format("%.2f h", report.getGlobal().getTotalRegularHours()), lightGray);
            addSummaryRow(globalTable, "Extras Diurnas", String.format("%.2f h", report.getGlobal().getTotalDiurnaExtraHours()), null);
            addSummaryRow(globalTable, "Horas Dominicales", String.format("%.2f h", report.getGlobal().getTotalDominicalHours()), lightGray);
            addSummaryRow(globalTable, "Horas Festivas", String.format("%.2f h", report.getGlobal().getTotalFestivoHours()), null);

            document.add(globalTable);

            // Locations summary
            document.add(new Paragraph("Resumen por Sede").setFontSize(14).setFontColor(ColorConstants.RED).setMarginTop(15));
            Table locationTable = new Table(UnitValue.createPercentArray(new float[]{40, 15, 15, 15, 15}));
            locationTable.setWidth(UnitValue.createPercentValue(100));
            
            locationTable.addHeaderCell(createHeaderCell("Sede", lightRed));
            locationTable.addHeaderCell(createHeaderCell("Empleados", lightRed));
            locationTable.addHeaderCell(createHeaderCell("Horas Totales", lightRed));
            locationTable.addHeaderCell(createHeaderCell("Horas Extras", lightRed));
            locationTable.addHeaderCell(createHeaderCell("Turnos", lightRed));

            if (report.getLocations() != null) {
                boolean alternate = false;
                for (LocationReportDto loc : report.getLocations()) {
                    Color rowColor = alternate ? lightGray : ColorConstants.WHITE;
                    locationTable.addCell(createBodyCell(loc.getLocationName(), rowColor));
                    locationTable.addCell(createBodyCell(String.valueOf(loc.getTotalEmployees()), rowColor));
                    locationTable.addCell(createBodyCell(String.format("%.1f", loc.getTotalHours()), rowColor));
                    locationTable.addCell(createBodyCell(String.format("%.1f", loc.getTotalOvertimeHours()), rowColor));
                    locationTable.addCell(createBodyCell(String.valueOf(loc.getTotalShifts()), rowColor));
                    alternate = !alternate;
                }
            }
            document.add(locationTable);

            // Employees summary
            document.add(new Paragraph("Resumen por Empleado").setFontSize(14).setFontColor(ColorConstants.RED).setMarginTop(15));
            Table employeeTable = new Table(UnitValue.createPercentArray(new float[]{35, 12, 12, 12, 12, 12, 12}));
            employeeTable.setWidth(UnitValue.createPercentValue(100));
            employeeTable.setFontSize(8);
            
            employeeTable.addHeaderCell(createHeaderCell("Empleado", lightRed));
            employeeTable.addHeaderCell(createHeaderCell("Horas Totales", lightRed));
            employeeTable.addHeaderCell(createHeaderCell("Regulares", lightRed));
            employeeTable.addHeaderCell(createHeaderCell("Extras", lightRed));
            employeeTable.addHeaderCell(createHeaderCell("Diurnas", lightRed));
            employeeTable.addHeaderCell(createHeaderCell("Nocturnas", lightRed));
            employeeTable.addHeaderCell(createHeaderCell("Turnos", lightRed));

            if (report.getEmployees() != null) {
                boolean alternate = false;
                for (EmployeeReportDto emp : report.getEmployees()) {
                    Color rowColor = alternate ? lightGray : ColorConstants.WHITE;
                    employeeTable.addCell(createBodyCell(emp.getFullName(), rowColor));
                    employeeTable.addCell(createBodyCell(String.format("%.1f", emp.getTotalHours()), rowColor));
                    employeeTable.addCell(createBodyCell(String.format("%.1f", emp.getRegularHours()), rowColor));
                    employeeTable.addCell(createBodyCell(String.format("%.1f", emp.getOvertimeHours()), rowColor));
                    employeeTable.addCell(createBodyCell(String.format("%.1f", emp.getDiurnaExtraHours()), rowColor));
                    employeeTable.addCell(createBodyCell(String.format("%.1f", emp.getNocturnaExtraHours()), rowColor));
                    employeeTable.addCell(createBodyCell(String.valueOf(emp.getTotalShifts()), rowColor));
                    alternate = !alternate;
                }
            }
            document.add(employeeTable);

            addDocumentFooter(document);

        } finally {
            document.flush();
            document.close();
        }

        return baos.toByteArray();
    }

    private byte[] generateLocationPdf(ReportResponseDto report, int month, int year, String locationName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(36, 36, 36, 36);

        try {
            Color lightRed = new DeviceRgb(255, 235, 235);
            Color lightGray = new DeviceRgb(245, 245, 245);
            
            // Header
            addDocumentHeader(document, "REPORTE POR SEDE: " + locationName.toUpperCase(), month, year);

            // Location summary
            if (report.getLocations() != null && !report.getLocations().isEmpty()) {
                LocationReportDto location = report.getLocations().get(0);
                document.add(new Paragraph("Datos de la Sede").setFontSize(14).setFontColor(ColorConstants.RED).setMarginTop(10));
                
                Table locTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
                locTable.setWidth(UnitValue.createPercentValue(80));
                
                locTable.addCell(createLabelCell("Sede:", lightGray));
                locTable.addCell(createBodyCell(location.getLocationName(), null));
                locTable.addCell(createLabelCell("Total Empleados:", lightGray));
                locTable.addCell(createBodyCell(String.valueOf(location.getTotalEmployees()), null));
                locTable.addCell(createLabelCell("Total Horas:", lightGray));
                locTable.addCell(createBodyCell(String.format("%.2f h", location.getTotalHours()), null));
                locTable.addCell(createLabelCell("Horas Extras Totales:", lightGray));
                locTable.addCell(createBodyCell(String.format("%.2f h", location.getTotalOvertimeHours()), null));
                locTable.addCell(createLabelCell("Horas Regulares:", lightGray));
                locTable.addCell(createBodyCell(String.format("%.2f h", location.getTotalRegularHours()), null));
                locTable.addCell(createLabelCell("Total Turnos:", lightGray));
                locTable.addCell(createBodyCell(String.valueOf(location.getTotalShifts()), null));
                
                document.add(locTable);

                // Employees at location
                if (location.getEmployeeReports() != null && !location.getEmployeeReports().isEmpty()) {
                    document.add(new Paragraph("Empleados en esta Sede").setFontSize(14).setFontColor(ColorConstants.RED).setMarginTop(15));
                    
                    Table empTable = new Table(UnitValue.createPercentArray(new float[]{40, 15, 15, 15, 15}));
                    empTable.setWidth(UnitValue.createPercentValue(100));
                    empTable.setFontSize(9);
                    
                    empTable.addHeaderCell(createHeaderCell("Empleado", lightRed));
                    empTable.addHeaderCell(createHeaderCell("Horas Totales", lightRed));
                    empTable.addHeaderCell(createHeaderCell("Extras", lightRed));
                    empTable.addHeaderCell(createHeaderCell("Regulares", lightRed));
                    empTable.addHeaderCell(createHeaderCell("Turnos", lightRed));

                    boolean alternate = false;
                    for (EmployeeReportDto emp : location.getEmployeeReports()) {
                        Color rowColor = alternate ? lightGray : ColorConstants.WHITE;
                        empTable.addCell(createBodyCell(emp.getFullName(), rowColor));
                        empTable.addCell(createBodyCell(String.format("%.1f", emp.getTotalHours()), rowColor));
                        empTable.addCell(createBodyCell(String.format("%.1f", emp.getOvertimeHours()), rowColor));
                        empTable.addCell(createBodyCell(String.format("%.1f", emp.getRegularHours()), rowColor));
                        empTable.addCell(createBodyCell(String.valueOf(emp.getTotalShifts()), rowColor));
                        alternate = !alternate;
                    }
                    document.add(empTable);
                }
            }

            addDocumentFooter(document);

        } finally {
            document.flush();
            document.close();
        }

        return baos.toByteArray();
    }

    private byte[] generateEmployeePdf(ReportResponseDto report, int month, int year, String employeeName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(36, 36, 36, 36);

        try {
            Color lightRed = new DeviceRgb(255, 235, 235);
            Color lightGray = new DeviceRgb(245, 245, 245);
            
            // Header
            addDocumentHeader(document, "REPORTE INDIVIDUAL DE EMPLEADO", month, year);
            document.add(new Paragraph("Empleado: " + employeeName).setFontSize(14).setFontColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            if (report.getEmployees() != null && !report.getEmployees().isEmpty()) {
                EmployeeReportDto emp = report.getEmployees().get(0);
                
                // Employee summary
                document.add(new Paragraph("Resumen de Horas").setFontSize(14).setFontColor(ColorConstants.RED).setMarginTop(10));
                
                Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
                summaryTable.setWidth(UnitValue.createPercentValue(80));
                
                summaryTable.addCell(createLabelCell("Horas Totales:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h", emp.getTotalHours()), null));
                summaryTable.addCell(createLabelCell("Horas Regulares:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h", emp.getRegularHours()), null));
                summaryTable.addCell(createLabelCell("Horas Extras Totales:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h", emp.getOvertimeHours()), null));
                summaryTable.addCell(createLabelCell("Extras Diurnas:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h", emp.getDiurnaExtraHours()), null));
                summaryTable.addCell(createLabelCell("Extras Nocturnas:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h", emp.getNocturnaExtraHours()), null));
                summaryTable.addCell(createLabelCell("Horas Dominicales:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h", emp.getDominicalHours()), null));
                summaryTable.addCell(createLabelCell("Horas Festivas:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h", emp.getFestivoHours()), null));
                summaryTable.addCell(createLabelCell("Promedio Diario:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h/día", emp.getDailyAvgHours()), null));
                summaryTable.addCell(createLabelCell("Total Semanal:", lightGray));
                summaryTable.addCell(createBodyCell(String.format("%.2f h/semana", emp.getWeeklyTotalHours()), null));
                summaryTable.addCell(createLabelCell("Total Turnos:", lightGray));
                summaryTable.addCell(createBodyCell(String.valueOf(emp.getTotalShifts()), null));
                
                document.add(summaryTable);

                // Overtime details
                if (emp.getOvertimeDetails() != null && !emp.getOvertimeDetails().isEmpty()) {
                    document.add(new Paragraph("Detalles de Horas Extras").setFontSize(14).setFontColor(ColorConstants.RED).setMarginTop(15));
                    
                    Table otTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}));
                    otTable.setWidth(UnitValue.createPercentValue(80));
                    
                    otTable.addHeaderCell(createHeaderCell("Fecha", lightRed));
                    otTable.addHeaderCell(createHeaderCell("Horas", lightRed));
                    otTable.addHeaderCell(createHeaderCell("Justificación", lightRed));

                    boolean alternate = false;
                    for (OvertimeDetailDto ot : emp.getOvertimeDetails()) {
                        Color rowColor = alternate ? lightGray : ColorConstants.WHITE;
                        otTable.addCell(createBodyCell(ot.getDate() != null ? ot.getDate().toString() : "", rowColor));
                        otTable.addCell(createBodyCell(String.format("%.2f h", ot.getHours()), rowColor));
                        otTable.addCell(createBodyCell(ot.getJustification() != null ? ot.getJustification() : "", rowColor));
                        alternate = !alternate;
                    }
                    document.add(otTable);
                }
            }

            addDocumentFooter(document);

        } finally {
            document.flush();
            document.close();
        }

        return baos.toByteArray();
    }

    // ==================== HELPER METHODS ====================

    private void addDocumentHeader(Document document, String title, int month, int year) {
        Paragraph header = new Paragraph("Microfarma Horarios")
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5)
                .setFontColor(new DeviceRgb(200, 0, 0));
        document.add(header);

        Paragraph subtitle = new Paragraph("Sistema de Gestión de Horarios Laborales")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10)
                .setFontColor(ColorConstants.GRAY);
        document.add(subtitle);

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        Paragraph reportTitle = new Paragraph(title + " - " + monthName + " " + year)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15)
                .setFontColor(ColorConstants.DARK_GRAY);
        document.add(reportTitle);
    }

    private void addDocumentFooter(Document document) {
        document.add(new Paragraph("").setMarginTop(20));
        document.add(new Paragraph("Microfarma")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(200, 0, 0)));
        document.add(new Paragraph("Reporte generado automáticamente por el sistema Microfarma Horarios")
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5)
                .setFontColor(ColorConstants.GRAY));
        document.add(new Paragraph("Fecha de generación: " + LocalDate.now())
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private Cell createHeaderCell(String text, Color backgroundColor) {
        Cell cell = new Cell().add(new Paragraph(text).setFontSize(9).setFontColor(ColorConstants.BLACK));
        cell.setBackgroundColor(backgroundColor);
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setPadding(5);
        return cell;
    }

    private Cell createBodyCell(String text, Color backgroundColor) {
        Cell cell = new Cell().add(new Paragraph(text).setFontSize(8));
        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setPadding(3);
        return cell;
    }

    private Cell createLabelCell(String text, Color backgroundColor) {
        Cell cell = new Cell().add(new Paragraph(text).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY));
        cell.setBackgroundColor(backgroundColor);
        cell.setTextAlignment(TextAlignment.LEFT);
        cell.setPadding(5);
        return cell;
    }

    private void addSummaryRow(Table table, String label, Object value, Color backgroundColor) {
        table.addCell(createLabelCell(label, backgroundColor));
        if (value instanceof Number) {
            table.addCell(createBodyCell(value.toString(), backgroundColor));
        } else {
            table.addCell(createBodyCell((String) value, backgroundColor));
        }
    }

    private String generateCsv(ReportResponseDto report, int month, int year) {
        StringBuilder csv = new StringBuilder();

        csv.append("Microfarma Horarios - Reporte Mensual de Horas\n");
        csv.append("Generado el,").append(LocalDate.now()).append("\n\n");

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

        csv.append("DETALLES POR EMPLEADO\n");
        csv.append("Nombre Completo,Horas Totales,Horas Regulares,Horas Extras Totales,Extras Diurnas,Extras Nocturnas,Horas Dominicales,Horas Festivas,Promedio Diario,Total Semanal,Total Turnos\n");

        if (report.getEmployees() != null) {
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
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
