package MicrofarmaHorarios.Schedules.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Month;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Schedules.DTO.Response.EmployeeReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.GlobalReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.LocationReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;

/**
 * Servicio para generar informes en Excel con diseño profesional
 * Incluye paletas de colores armoniosas, bordes definidos,
 * encabezados destacados y filas alternadas
 */
@Service
public class ExcelExportService {

    // ============================================
    // PALETA DE COLORES PROFESIONAL - Microfarma
    // ============================================
    
    // Colores primarios - Azul corporativo profesional
    private static final Color PRIMARY_BLUE = new Color(0, 51, 102);        // Azul oscuro corporativo
    private static final Color PRIMARY_BLUE_LIGHT = new Color(0, 82, 153);   // Azul medio
    private static final Color PRIMARY_BLUE_EXTRALIGHT = new Color(51, 122, 183); // Azul claro
    
    // Colores secundarios - Verde azulado para acentos
    private static final Color ACCENT_TEAL = new Color(0, 128, 128);        // Verde azulado
    private static final Color ACCENT_TEAL_LIGHT = new Color(72, 176, 146);  // Verde azulado claro
    
    // Colores нейтральные - Grises profesionales
    private static final Color GRAY_DARK = new Color(64, 64, 64);           // Gris oscuro
    private static final Color GRAY_MEDIUM = new Color(128, 128, 128);       // Gris medio
    private static final Color GRAY_LIGHT = new Color(217, 217, 217);        // Gris claro
    private static final Color GRAY_EXTRALIGHT = new Color(245, 245, 245);   // Gris muy claro
    
    // Colores para filas alternadas
    private static final Color ROW_EVEN = Color.WHITE;                      // Blanco para filas pares
    private static final Color ROW_ODD = new Color(240, 248, 255);          // Azul muy claro AliceBlue
    
    // Colores para estados/condiciones
    private static final Color SUCCESS_GREEN = new Color(34, 139, 34);       // Verde éxito
    private static final Color WARNING_ORANGE = new Color(255, 140, 0);      // Naranja advertencia
    private static final Color DANGER_RED = new Color(220, 20, 60);          // Rojo peligro
    
    // Colores de encabezado
    private static final Color HEADER_BLUE = new Color(0, 82, 153);         // Azul encabezado
    private static final Color HEADER_BLUE_DARK = new Color(0, 51, 102);     // Azul oscuro
    
    // ============================================
    // MÉTODO PRINCIPAL DE EXPORTACIÓN
    // ============================================
    
    /**
     * Genera un informe completo en Excel con diseño profesional
     * @param report Datos del informe
     * @param month Mes del informe
     * @param year Año del informe
     * @return Array de bytes del archivo Excel
     */
    public byte[] generateProfessionalExcelReport(ReportResponseDto report, int month, int year) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Configurar márgenes del libro (aplicar a cada hoja)
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                sheet.setMargin(Sheet.LeftMargin, 0.75);
                sheet.setMargin(Sheet.RightMargin, 0.75);
                sheet.setMargin(Sheet.TopMargin, 1.0);
                sheet.setMargin(Sheet.BottomMargin, 1.0);
            }
            
            // Crear hojas del informe
            createGlobalSummarySheet(workbook, report.getGlobal(), month, year);
            createLocationsSheet(workbook, report.getLocations(), month, year);
            createEmployeesSheet(workbook, report.getEmployees(), month, year);
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    // ============================================
    // HOJA 1: RESUMEN GLOBAL
    // ============================================
    
    private void createGlobalSummarySheet(Workbook workbook, GlobalReportDto global, int month, int year) {
        Sheet sheet = workbook.createSheet("Resumen Global");
        
        // Configurar ancho de columnas
        sheet.setColumnWidth(0, 5000);  // Descripción
        sheet.setColumnWidth(1, 3000);  // Valor
        sheet.setColumnWidth(2, 4000);  // Notas
        
        // Configurar márgenes
        sheet.setMargin(Sheet.LeftMargin, 0.75);
        sheet.setMargin(Sheet.RightMargin, 0.75);
        sheet.setMargin(Sheet.TopMargin, 1.0);
        sheet.setMargin(Sheet.BottomMargin, 1.0);
        
        // Crear estilos
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyleEven = createDataStyle(workbook, ROW_EVEN);
        CellStyle dataStyleOdd = createDataStyle(workbook, ROW_ODD);
        CellStyle highlightStyle = createHighlightStyle(workbook);
        CellStyle percentageStyle = createPercentageStyle(workbook);
        
        int rowNum = 0;
        
        // Título principal
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("INFORME DE HORARIOS - MICROFARMA");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        
        // Subtítulo con período
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        String monthName = Month.of(month).name();
        subtitleCell.setCellValue("Período: " + monthName + " " + year);
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
        
        rowNum++; // Fila vacía
        
        // Sección: Estadísticas Generales
        Row sectionTitleRow = sheet.createRow(rowNum++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("ESTADÍSTICAS GENERALES");
        sectionTitleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));
        
        // Encabezados de tabla
        Row headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "MÉTRICA", headerStyle);
        createHeaderCell(headerRow, 1, "VALOR", headerStyle);
        createHeaderCell(headerRow, 2, "OBSERVACIONES", headerStyle);
        
        // Datos de métricas generales
        Object[][] globalData = {
            {"Total Empleados", global.getTotalEmployees(), "Colaboradores activos en el período"},
            {"Total Turnos", global.getTotalShifts(), "Turnos completados"},
            {"Horas Totales", global.getTotalHours(), "Horas laboradas acumuladas"},
            {"Horas Extras", global.getTotalOvertimeHours(), "Horas adicionales trabajadas"},
            {"Horas Promedio por Empleado", 
             global.getTotalEmployees() > 0 ? global.getTotalHours() / global.getTotalEmployees() : 0, 
             "Media de horas por colaborador"}
        };
        
        for (int i = 0; i < globalData.length; i++) {
            CellStyle style = (i % 2 == 0) ? dataStyleEven : dataStyleOdd;
            Row dataRow = sheet.createRow(rowNum++);
            createDataCell(dataRow, 0, globalData[i][0].toString(), style);
            createDataCell(dataRow, 1, formatNumber(globalData[i][1]), style);
            createDataCell(dataRow, 2, globalData[i][2].toString(), style);
        }
        
        rowNum++; // Fila vacía
        
        // Sección: Desglose de Horas
        Row breakdownSectionRow = sheet.createRow(rowNum++);
        Cell breakdownSectionCell = breakdownSectionRow.createCell(0);
        breakdownSectionCell.setCellValue("DESGLOSE DE HORAS POR CATEGORÍA");
        breakdownSectionCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));
        
        // Encabezados desglose
        Row breakdownHeaderRow = sheet.createRow(rowNum++);
        createHeaderCell(breakdownHeaderRow, 0, "TIPO DE HORAS", headerStyle);
        createHeaderCell(breakdownHeaderRow, 1, "CANTIDAD", headerStyle);
        createHeaderCell(breakdownHeaderRow, 2, "PORCENTAJE", headerStyle);
        
        // Calcular totales y porcentajes
        double totalHours = global.getTotalHours() != null ? global.getTotalHours() : 0;
        
        Object[][] breakdownData = {
            {"Horas Regulares", global.getTotalRegularHours(), calculatePercentage(global.getTotalRegularHours(), totalHours)},
            {"Horas Extras Diurnas", global.getTotalDiurnaExtraHours(), calculatePercentage(global.getTotalDiurnaExtraHours(), totalHours)},
            {"Horas Extras Nocturnas", global.getTotalNocturnaExtraHours(), calculatePercentage(global.getTotalNocturnaExtraHours(), totalHours)},
            {"Horas Dominicales", global.getTotalDominicalHours(), calculatePercentage(global.getTotalDominicalHours(), totalHours)},
            {"Horas Festivas", global.getTotalFestivoHours(), calculatePercentage(global.getTotalFestivoHours(), totalHours)}
        };
        
        for (int i = 0; i < breakdownData.length; i++) {
            CellStyle style = (i % 2 == 0) ? dataStyleEven : dataStyleOdd;
            Row dataRow = sheet.createRow(rowNum++);
            createDataCell(dataRow, 0, breakdownData[i][0].toString(), style);
            createDataCell(dataRow, 1, formatNumber(breakdownData[i][1]), style);
            
            Cell percentCell = dataRow.createCell(2);
            percentCell.setCellValue((Double) breakdownData[i][2]);
            percentCell.setCellStyle(percentageStyle);
        }
        
        // Fila de total
        Row totalRow = sheet.createRow(rowNum++);
        CellStyle totalStyle = createTotalStyle(workbook);
        createDataCell(totalRow, 0, "TOTAL", totalStyle);
        createDataCell(totalRow, 1, formatNumber(totalHours), totalStyle);
        createDataCell(totalRow, 2, "100%", totalStyle);
        
        rowNum++; // Fila vacía
        
        // Notas finales
        Row notesRow = sheet.createRow(rowNum++);
        Cell notesCell = notesRow.createCell(0);
        notesCell.setCellValue("Nota: Las horas extras diurnas son aquellas trabajadas entre 6:00 AM y 7:00 PM. Las horas extras nocturnas son las trabajadas entre 7:00 PM y 6:00 AM.");
        CellStyle notesStyle = workbook.createCellStyle();
        Font notesFont = workbook.createFont();
        notesFont.setItalic(true);
        notesFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        notesFont.setFontHeightInPoints((short) 9);
        notesStyle.setFont(notesFont);
        notesStyle.setAlignment(HorizontalAlignment.LEFT);
        notesCell.setCellStyle(notesStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));
    }
    
    // ============================================
    // HOJA 2: INFORME POR UBICACIONES
    // ============================================
    
    private void createLocationsSheet(Workbook workbook, List<LocationReportDto> locations, int month, int year) {
        Sheet sheet = workbook.createSheet("Informe por Ubicaciones");
        
        // Configurar anchos de columnas
        sheet.setColumnWidth(0, 4000);  // Ubicación
        sheet.setColumnWidth(1, 2500); // Empleados
        sheet.setColumnWidth(2, 2500); // Turnos
        sheet.setColumnWidth(3, 3000); // Horas Totales
        sheet.setColumnWidth(4, 3000); // Horas Extras
        sheet.setColumnWidth(5, 3000); // Horas Regulares
        sheet.setColumnWidth(6, 3000); // H. Extra Diurna
        sheet.setColumnWidth(7, 3000); // H. Extra Nocturna
        sheet.setColumnWidth(8, 3000); // Dominicales
        sheet.setColumnWidth(9, 3000); // Festivas
        
        // Título
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("INFORME DE HORAS POR UBICACIÓN - " + Month.of(month).name() + " " + year);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
        
        // Encabezados de columna
        Row headerRow = sheet.createRow(2);
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        String[] headers = {
            "UBICACIÓN", "EMPLEADOS", "TURNOS", "HORAS TOTALES", 
            "HORAS EXTRAS", "HORAS REGULARES", "EXTRA DIURNA", 
            "EXTRA NOCTURNA", "DOMINICALES", "FESTIVAS"
        };
        
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }
        
        // Datos de ubicaciones
        int rowNum = 3;
        CellStyle styleEven = createDataStyle(workbook, ROW_EVEN);
        CellStyle styleOdd = createDataStyle(workbook, ROW_ODD);
        
        for (int i = 0; i < locations.size(); i++) {
            LocationReportDto location = locations.get(i);
            CellStyle style = (rowNum % 2 == 0) ? styleEven : styleOdd;
            
            Row dataRow = sheet.createRow(rowNum++);
            
            createDataCell(dataRow, 0, location.getLocationName(), style);
            createDataCell(dataRow, 1, location.getTotalEmployees(), style);
            createDataCell(dataRow, 2, location.getTotalShifts(), style);
            createDataCell(dataRow, 3, formatNumber(location.getTotalHours()), style);
            createDataCell(dataRow, 4, formatNumber(location.getTotalOvertimeHours()), style);
            createDataCell(dataRow, 5, formatNumber(location.getTotalRegularHours()), style);
            createDataCell(dataRow, 6, formatNumber(location.getTotalDiurnaExtraHours()), style);
            createDataCell(dataRow, 7, formatNumber(location.getTotalNocturnaExtraHours()), style);
            createDataCell(dataRow, 8, formatNumber(location.getTotalDominicalHours()), style);
            createDataCell(dataRow, 9, formatNumber(location.getTotalFestivoHours()), style);
        }
        
        // Fila de totales
        if (!locations.isEmpty()) {
            Row totalRow = sheet.createRow(rowNum);
            CellStyle totalStyle = createTotalStyle(workbook);
            
            createDataCell(totalRow, 0, "TOTALES", totalStyle);
            createDataCell(totalRow, 1, locations.stream().mapToInt(LocationReportDto::getTotalEmployees).sum(), totalStyle);
            createDataCell(totalRow, 2, locations.stream().mapToInt(LocationReportDto::getTotalShifts).sum(), totalStyle);
            createDataCell(totalRow, 3, formatNumber(locations.stream().mapToDouble(LocationReportDto::getTotalHours).sum()), totalStyle);
            createDataCell(totalRow, 4, formatNumber(locations.stream().mapToDouble(LocationReportDto::getTotalOvertimeHours).sum()), totalStyle);
            createDataCell(totalRow, 5, formatNumber(locations.stream().mapToDouble(LocationReportDto::getTotalRegularHours).sum()), totalStyle);
            createDataCell(totalRow, 6, formatNumber(locations.stream().mapToDouble(LocationReportDto::getTotalDiurnaExtraHours).sum()), totalStyle);
            createDataCell(totalRow, 7, formatNumber(locations.stream().mapToDouble(LocationReportDto::getTotalNocturnaExtraHours).sum()), totalStyle);
            createDataCell(totalRow, 8, formatNumber(locations.stream().mapToDouble(LocationReportDto::getTotalDominicalHours).sum()), totalStyle);
            createDataCell(totalRow, 9, formatNumber(locations.stream().mapToDouble(LocationReportDto::getTotalFestivoHours).sum()), totalStyle);
        }
        
        // Aplicar formato condicional para horas extras excesivas
        applyConditionalFormatting(sheet, 3, rowNum - 1, 4, 50.0);
    }
    
    // ============================================
    // HOJA 3: INFORME POR EMPLEADOS
    // ============================================
    
    private void createEmployeesSheet(Workbook workbook, List<EmployeeReportDto> employees, int month, int year) {
        Sheet sheet = workbook.createSheet("Informe por Empleado");
        
        // Configurar anchos de columnas
        sheet.setColumnWidth(0, 3000);  // ID
        sheet.setColumnWidth(1, 5000);  // Nombre
        sheet.setColumnWidth(2, 2500); // Días Trab.
        sheet.setColumnWidth(3, 2500); // Turnos
        sheet.setColumnWidth(4, 3000); // Horas Totales
        sheet.setColumnWidth(5, 3000); // Horas/Día
        sheet.setColumnWidth(6, 3000); // Horas/Semana
        sheet.setColumnWidth(7, 3000); // Horas Extras
        sheet.setColumnWidth(8, 3000); // Horas Regulares
        sheet.setColumnWidth(9, 3000); // Extra Diurna
        sheet.setColumnWidth(10, 3000); // Extra Nocturna
        sheet.setColumnWidth(11, 3000); // Dominicales
        sheet.setColumnWidth(12, 3000); // Festivas
        
        // Título
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("INFORME INDIVIDUAL DE HORARIOS - " + Month.of(month).name() + " " + year);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));
        
        // Encabezados
        Row headerRow = sheet.createRow(2);
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        String[] headers = {
            "ID EMPLEADO", "NOMBRE COMPLETO", "DÍAS LAB.", "TURNOS", 
            "HORAS TOTALES", "PROM/DÍA", "PROM/SEMANA", 
            "HORAS EXTRAS", "REGULARES", "EXTRA DIURNA", 
            "EXTRA NOCTURNA", "DOMINICALES", "FESTIVAS"
        };
        
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }
        
        // Datos de empleados
        int rowNum = 3;
        CellStyle styleEven = createDataStyle(workbook, ROW_EVEN);
        CellStyle styleOdd = createDataStyle(workbook, ROW_ODD);
        CellStyle overtimeWarningStyle = createOvertimeWarningStyle(workbook);
        
        for (int i = 0; i < employees.size(); i++) {
            EmployeeReportDto emp = employees.get(i);
            CellStyle style = (rowNum % 2 == 0) ? styleEven : styleOdd;
            
            Row dataRow = sheet.createRow(rowNum++);
            
            createDataCell(dataRow, 0, emp.getEmployeeId(), style);
            createDataCell(dataRow, 1, emp.getFullName(), style);
            createDataCell(dataRow, 2, emp.getWorkingDays(), style);
            createDataCell(dataRow, 3, emp.getTotalShifts(), style);
            createDataCell(dataRow, 4, formatNumber(emp.getTotalHours()), style);
            createDataCell(dataRow, 5, formatNumber(emp.getDailyAvgHours()), style);
            createDataCell(dataRow, 6, formatNumber(emp.getWeeklyTotalHours()), style);
            
            // Aplicar estilo de advertencia si hay muchas horas extras
            CellStyle overtimeStyle = emp.getOvertimeHours() != null && emp.getOvertimeHours() > 20 ? overtimeWarningStyle : style;
            createDataCell(dataRow, 7, formatNumber(emp.getOvertimeHours()), overtimeStyle);
            
            createDataCell(dataRow, 8, formatNumber(emp.getRegularHours()), style);
            createDataCell(dataRow, 9, formatNumber(emp.getDiurnaExtraHours()), style);
            createDataCell(dataRow, 10, formatNumber(emp.getNocturnaExtraHours()), style);
            createDataCell(dataRow, 11, formatNumber(emp.getDominicalHours()), style);
            createDataCell(dataRow, 12, formatNumber(emp.getFestivoHours()), style);
        }
        
        // Fila de totales
        if (!employees.isEmpty()) {
            Row totalRow = sheet.createRow(rowNum);
            CellStyle totalStyle = createTotalStyle(workbook);
            
            createDataCell(totalRow, 0, "TOTALES", totalStyle);
            createDataCell(totalRow, 1, employees.size() + " empleados", totalStyle);
            createDataCell(totalRow, 2, employees.stream().mapToInt(e -> e.getWorkingDays() != null ? e.getWorkingDays() : 0).sum(), totalStyle);
            createDataCell(totalRow, 3, employees.stream().mapToInt(e -> e.getTotalShifts() != null ? e.getTotalShifts() : 0).sum(), totalStyle);
            createDataCell(totalRow, 4, formatNumber(employees.stream().mapToDouble(e -> e.getTotalHours() != null ? e.getTotalHours() : 0).sum()), totalStyle);
            createDataCell(totalRow, 5, "-", totalStyle);
            createDataCell(totalRow, 6, "-", totalStyle);
            createDataCell(totalRow, 7, formatNumber(employees.stream().mapToDouble(e -> e.getOvertimeHours() != null ? e.getOvertimeHours() : 0).sum()), totalStyle);
            createDataCell(totalRow, 8, formatNumber(employees.stream().mapToDouble(e -> e.getRegularHours() != null ? e.getRegularHours() : 0).sum()), totalStyle);
            createDataCell(totalRow, 9, formatNumber(employees.stream().mapToDouble(e -> e.getDiurnaExtraHours() != null ? e.getDiurnaExtraHours() : 0).sum()), totalStyle);
            createDataCell(totalRow, 10, formatNumber(employees.stream().mapToDouble(e -> e.getNocturnaExtraHours() != null ? e.getNocturnaExtraHours() : 0).sum()), totalStyle);
            createDataCell(totalRow, 11, formatNumber(employees.stream().mapToDouble(e -> e.getDominicalHours() != null ? e.getDominicalHours() : 0).sum()), totalStyle);
            createDataCell(totalRow, 12, formatNumber(employees.stream().mapToDouble(e -> e.getFestivoHours() != null ? e.getFestivoHours() : 0).sum()), totalStyle);
        }
        
        // Formato condicional para horas extras > 20
        applyConditionalFormatting(sheet, 3, rowNum - 1, 7, 20.0);
    }
    
    // ============================================
    // MÉTODOS DE ESTILO - TÍTULOS Y ENCABEZADOS
    // ============================================
    
    /**
     * Crea estilo para títulos principales
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        font.setFontName("Calibri");
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Borde inferior doble
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBottomBorderColor(IndexedColors.DARK_BLUE.getIndex());
        
        return style;
    }
    
    /**
     * Crea estilo para subtítulos
     */
    private CellStyle createSubtitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setFontName("Calibri");
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * Crea estilo para encabezados de tabla
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Fondo azul corporativo
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Fuente blanca y negrita
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("Calibri");
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Bordes completos
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        
        // Ajuste de texto
        style.setWrapText(true);
        
        return style;
    }
    
    // ============================================
    // MÉTODOS DE ESTILO - DATOS
    // ============================================
    
    /**
     * Crea estilo para celdas de datos
     */
    private CellStyle createDataStyle(Workbook workbook, Color backgroundColor) {
        CellStyle style = workbook.createCellStyle();
        
        // Fondo con color alternado
        if (backgroundColor.equals(ROW_EVEN)) {
            style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        } else {
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Fuente
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        font.setColor(IndexedColors.BLACK.getIndex());
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Bordes completos
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        return style;
    }
    
    /**
     * Crea estilo para celdas destacadas
     */
    private CellStyle createHighlightStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Bordes
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        
        return style;
    }
    
    /**
     * Crea estilo para porcentajes
     */
    private CellStyle createPercentageStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Bordes
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * Crea estilo para fila de totales
     */
    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Fondo gris claro
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Fuente negrita
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Calibri");
        font.setColor(IndexedColors.BLACK.getIndex());
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Bordes completos más gruesos
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        
        return style;
    }
    
    /**
     * Crea estilo para advertencia de horas extras excesivas
     */
    private CellStyle createOvertimeWarningStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Fondo naranja claro
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Fuente roja oscura
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        font.setColor(IndexedColors.DARK_RED.getIndex());
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Bordes
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================
    
    /**
     * Crea una celda de encabezado
     */
    private void createHeaderCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
    
    /**
     * Crea una celda de datos con valor numérico
     */
    private void createDataCell(Row row, int column, Number value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }
    
    /**
     * Crea una celda de datos con valor de texto
     */
    private void createDataCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    /**
     * Formatea un número para mostrar
     */
    private String formatNumber(Object value) {
        if (value == null) {
            return "0.00";
        }
        if (value instanceof Number) {
            Number number = (Number) value;
            if (number instanceof Double || number instanceof Float) {
                return String.format("%.2f", number.doubleValue());
            }
            return number.toString();
        }
        return value.toString();
    }
    
    /**
     * Calcula el porcentaje de un valor respecto al total
     */
    private Double calculatePercentage(Number value, double total) {
        if (value == null || total == 0) {
            return 0.0;
        }
        return (value.doubleValue() / total) * 100;
    }
    
    /**
     * Aplica formato condicional a una columna
     * Resalta celdas que superen un umbral específico
     */
    private void applyConditionalFormatting(Sheet sheet, int startRow, int endRow, int column, double threshold) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        
        // Crear validación de datos usando fórmula personalizada
        CellRangeAddressList cellRangeList = new CellRangeAddressList(startRow, endRow, column, column);
        
        // Usar createFormulaConstraint con fórmula personalizada (un solo argumento)
        // La fórmula es relativa a la primera celda del rango
        String formula = getExcelColumnLetter(column) + "1>" + threshold;
        DataValidationConstraint constraint = validationHelper.createCustomConstraint(formula);
        
        DataValidation validation = validationHelper.createValidation(constraint, cellRangeList);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        
        sheet.addValidationData(validation);
    }
    
    /**
     * Convierte número de columna a letra de Excel (0=A, 1=B, ..., 26=AA, etc.)
     */
    private String getExcelColumnLetter(int column) {
        StringBuilder sb = new StringBuilder();
        while (column >= 0) {
            sb.insert(0, (char) ('A' + (column % 26)));
            column = column / 26 - 1;
        }
        return sb.toString();
    }
}
