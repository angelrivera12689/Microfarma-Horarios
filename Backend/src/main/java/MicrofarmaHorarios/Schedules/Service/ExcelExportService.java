package MicrofarmaHorarios.Schedules.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Schedules.DTO.Response.EmployeeReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.GlobalReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.LocationReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;
import MicrofarmaHorarios.Schedules.Entity.Shift;

@Service
public class ExcelExportService {

    private static final double LIMITE_HORAS = 220.0;
    private static final Locale LOCALE_ES = new Locale("es", "CO");

    // =====================================================
    // EXPORTAR
    // =====================================================
    public byte[] generateProfessionalExcelReport(
            ReportResponseDto report,
            int month,
            int year,
            Double baseRate,
            Double regularHours,
            Double diurnaExtraHours,
            Double nocturnaExtraHours,
            Double dominicalHours,
            Double festivoHours,
            Double monthlyHourLimit) throws IOException {

        double limiteHoras = (monthlyHourLimit != null) ? monthlyHourLimit : LIMITE_HORAS;

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            createGlobalSheet(workbook, report.getGlobal(), month, year);
            createLocationsSheet(workbook, report.getLocations(), month, year);
            createEmployeesSheet(workbook, report.getEmployees(), month, year,
                    baseRate, regularHours, diurnaExtraHours, nocturnaExtraHours,
                    dominicalHours, festivoHours, limiteHoras);
            createAlertsSheet(workbook, report.getEmployees(), month, year, limiteHoras);

            workbook.getCreationHelper()
                    .createFormulaEvaluator()
                    .evaluateAll();

            autoSizeAllSheets(workbook);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =====================================================
    // RESUMEN
    // =====================================================
    private void createGlobalSheet(Workbook wb, GlobalReportDto g, int month, int year) {

        Sheet sheet = wb.createSheet("Resumen");
        configSheet(sheet);

        CellStyle title  = titleStyle(wb);
        CellStyle sub    = subTitleStyle(wb);
        CellStyle header = headerStyle(wb);
        CellStyle even   = dataStyle(wb, true);
        CellStyle odd    = dataStyle(wb, false);

        int row = 0;

        Row r0 = sheet.createRow(row++);
        createCell(r0, 0, "DROGUERÍA MICROFARMA", title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        Row r1 = sheet.createRow(row++);
        createCell(r1, 0, "Informe General de Horas", sub);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));

        Row r2 = sheet.createRow(row++);
        createCell(r2, 0, getMonth(month) + " " + year, sub);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 2));

        row++;

        Row h = sheet.createRow(row++);
        createCell(h, 0, "MÉTRICA", header);
        createCell(h, 1, "VALOR", header);
        createCell(h, 2, "DETALLE", header);

        Object[][] data = {
                {"Total Empleados",  g.getTotalEmployees(),     "Personal activo"},
                {"Total Turnos",     g.getTotalShifts(),        "Turnos registrados"},
                {"Horas Totales",    g.getTotalHours(),         "Horas trabajadas"},
                {"Horas Extras",     g.getTotalOvertimeHours(), "Horas adicionales"},
                {"Horas Regulares",  g.getTotalRegularHours(),  "Jornada normal"}
        };

        for (int i = 0; i < data.length; i++) {
            Row rw = sheet.createRow(row++);
            CellStyle st = (i % 2 == 0) ? even : odd;
            createCell(rw, 0, data[i][0].toString(), st);
            createCell(rw, 1, (Number) data[i][1], st);
            createCell(rw, 2, data[i][2].toString(), st);
        }
    }

    // =====================================================
    // UBICACIONES
    // =====================================================
    private void createLocationsSheet(Workbook wb, List<LocationReportDto> list, int month, int year) {

        Sheet sheet = wb.createSheet("Ubicaciones");
        configSheet(sheet);

        CellStyle title  = titleStyle(wb);
        CellStyle header = headerStyle(wb);
        CellStyle even   = dataStyle(wb, true);
        CellStyle odd    = dataStyle(wb, false);

        Row r0 = sheet.createRow(0);
        createCell(r0, 0, "HORAS POR UBICACIÓN - " + getMonth(month) + " " + year, title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        Row h = sheet.createRow(2);
        String[] headers = {
                "UBICACIÓN", "EMPLEADOS", "TURNOS", "HORAS",
                "EXTRAS", "REGULARES", "EXTRA D",
                "EXTRA N", "DOMINICAL", "FESTIVA"
        };
        for (int i = 0; i < headers.length; i++) {
            createCell(h, i, headers[i], header);
        }

        int row = 3;
        for (int i = 0; i < list.size(); i++) {
            LocationReportDto x = list.get(i);
            CellStyle st = (i % 2 == 0) ? even : odd;
            Row rw = sheet.createRow(row++);
            createCell(rw, 0, x.getLocationName(), st);
            createCell(rw, 1, x.getTotalEmployees(), st);
            createCell(rw, 2, x.getTotalShifts(), st);
            createCell(rw, 3, x.getTotalHours(), st);
            createCell(rw, 4, x.getTotalOvertimeHours(), st);
            createCell(rw, 5, x.getTotalRegularHours(), st);
            createCell(rw, 6, x.getTotalDiurnaExtraHours(), st);
            createCell(rw, 7, x.getTotalNocturnaExtraHours(), st);
            createCell(rw, 8, x.getTotalDominicalHours(), st);
            createCell(rw, 9, x.getTotalFestivoHours(), st);
        }
    }

    // =====================================================
    // EMPLEADOS
    // =====================================================
    private void createEmployeesSheet(
            Workbook wb,
            List<EmployeeReportDto> list,
            int month,
            int year,
            Double baseRate,
            Double regularHoursMult,
            Double diurnaExtraMult,
            Double nocturnaExtraMult,
            Double dominicalMult,
            Double festivoMult,
            Double limiteHoras) {

        Sheet sheet = wb.createSheet("Empleados");
        configSheet(sheet);
        sheet.createFreezePane(0, 3);

        CellStyle title  = titleStyle(wb);
        CellStyle header = headerStyle(wb);
        CellStyle even   = dataStyle(wb, true);
        CellStyle odd    = dataStyle(wb, false);
        CellStyle red    = redSoftStyle(wb);

        Row r0 = sheet.createRow(0);
        createCell(r0, 0, "INFORME DE EMPLEADOS - " + getMonth(month) + " " + year, title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

        Row h = sheet.createRow(2);
        String[] headers = {
                "NOMBRE", "DÍAS", "TURNOS",
                "HORAS", "PROM DÍA", "PROM SEM",
                "EXTRAS >LIM", "REGULARES",
                "EXTRA D", "EXTRA N",
                "DOMINICAL", "FESTIVA", "TOTAL A PAGAR"
        };
        for (int i = 0; i < headers.length; i++) {
            createCell(h, i, headers[i], header);
        }

        int row = 3;
        for (int i = 0; i < list.size(); i++) {
            EmployeeReportDto e = list.get(i);
            CellStyle st = (i % 2 == 0) ? even : odd;
            Row rw = sheet.createRow(row);

            createCell(rw, 0, e.getFullName(), st);
            createCell(rw, 1, e.getWorkingDays(), st);
            createCell(rw, 2, e.getTotalShifts(), st);
            createCell(rw, 3, e.getTotalHours(), st);
            createCell(rw, 4, e.getDailyAvgHours(), st);
            createCell(rw, 5, e.getWeeklyTotalHours(), st);

            Cell extra = rw.createCell(6);
            extra.setCellFormula("MAX(D" + (row + 1) + "-" + limiteHoras + ",0)");
            extra.setCellStyle(nvl(e.getTotalHours()) > limiteHoras ? red : st);

            createCell(rw, 7,  e.getRegularHours(), st);
            createCell(rw, 8,  e.getDiurnaExtraHours(), st);
            createCell(rw, 9,  e.getNocturnaExtraHours(), st);
            createCell(rw, 10, e.getDominicalHours(), st);
            createCell(rw, 11, e.getFestivoHours(), st);

            double totalToPay = calculateTotalToPay(e, baseRate, regularHoursMult,
                    diurnaExtraMult, nocturnaExtraMult, dominicalMult, festivoMult);
            Cell totalCell = rw.createCell(12);
            totalCell.setCellValue(totalToPay);
            totalCell.setCellStyle(createCurrencyStyle(wb));

            row++;
        }

        applyConditional(sheet, 3, row - 1, 6);
    }

    private double calculateTotalToPay(
            EmployeeReportDto e,
            Double baseRate,
            Double regularMult,
            Double diurnaExtraMult,
            Double nocturnaExtraMult,
            Double dominicalMult,
            Double festivoMult) {

        double br  = baseRate          != null ? baseRate          : 5000.0;
        double reg = regularMult       != null ? regularMult       : 1.0;
        double dex = diurnaExtraMult   != null ? diurnaExtraMult   : 1.35;
        double nex = nocturnaExtraMult != null ? nocturnaExtraMult : 1.50;
        double dom = dominicalMult     != null ? dominicalMult     : 1.75;
        double fes = festivoMult       != null ? festivoMult       : 1.75;

        double regularValue       = (e.getRegularHours()       != null ? e.getRegularHours()       : 0) * br * reg;
        double diurnaExtraValue   = (e.getDiurnaExtraHours()   != null ? e.getDiurnaExtraHours()   : 0) * br * dex;
        double nocturnaExtraValue = (e.getNocturnaExtraHours() != null ? e.getNocturnaExtraHours() : 0) * br * nex;
        double dominicalValue     = (e.getDominicalHours()     != null ? e.getDominicalHours()     : 0) * br * dom;
        double festivoValue       = (e.getFestivoHours()       != null ? e.getFestivoHours()       : 0) * br * fes;

        return regularValue + diurnaExtraValue + nocturnaExtraValue + dominicalValue + festivoValue;
    }

    private CellStyle createCurrencyStyle(Workbook wb) {
        CellStyle s = base(wb);
        DataFormat format = wb.createDataFormat();
        s.setDataFormat(format.getFormat("$#,##0.00"));
        return s;
    }

    // =====================================================
    // ALERTAS
    // =====================================================
    private void createAlertsSheet(
            Workbook wb,
            List<EmployeeReportDto> all,
            int month,
            int year,
            double limiteHoras) {

        Sheet sheet = wb.createSheet("Alertas");
        configSheet(sheet);

        CellStyle title  = titleStyle(wb);
        CellStyle header = grayHeaderStyle(wb);
        CellStyle normal = dataStyle(wb, true);
        CellStyle red    = redSoftStyle(wb);

        List<EmployeeReportDto> list = all.stream()
                .filter(x -> nvl(x.getTotalHours()) > limiteHoras)
                .collect(Collectors.toList());

        Row r0 = sheet.createRow(0);
        createCell(r0, 0, "ALERTA EMPLEADOS +" + (int) limiteHoras + " HORAS", title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        Row r1 = sheet.createRow(1);
        createCell(r1, 0, "Total empleados en alerta: " + list.size(), subTitleStyle(wb));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        Row h = sheet.createRow(3);
        String[] headers = {
                "NOMBRE", "HORAS", "EXTRAS",
                "PROM DÍA", "PROM SEM",
                "TURNOS", "DÍAS"
        };
        for (int i = 0; i < headers.length; i++) {
            createCell(h, i, headers[i], header);
        }

        int row = 4;
        for (EmployeeReportDto e : list) {
            Row rw = sheet.createRow(row);
            createCell(rw, 0, e.getFullName(), normal);
            createCell(rw, 1, e.getTotalHours(), normal);

            Cell c = rw.createCell(2);
            c.setCellFormula("MAX(B" + (row + 1) + "-" + (int) limiteHoras + ",0)");
            c.setCellStyle(red);

            createCell(rw, 3, e.getDailyAvgHours(), normal);
            createCell(rw, 4, e.getWeeklyTotalHours(), normal);
            createCell(rw, 5, e.getTotalShifts(), normal);
            createCell(rw, 6, e.getWorkingDays(), normal);
            row++;
        }
    }

    // =====================================================
    // LISTADO DE TURNOS
    // =====================================================
    public byte[] generateShiftsListExcel(List<Shift> shifts, int month, int year) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Turnos");
            configSheet(sheet);

            CellStyle title       = titleStyle(workbook);
            CellStyle header      = headerStyle(workbook);
            CellStyle even        = dataStyle(workbook, true);
            CellStyle odd         = dataStyle(workbook, false);
            CellStyle locationHdr = locationHeaderStyle(workbook);

            int row = 0;

            Row r0 = sheet.createRow(row++);
            createCell(r0, 0, "LISTADO DE TURNOS - " + getMonth(month) + " " + year, title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            row++;

            Row h = sheet.createRow(row++);
            String[] headers = {"SEDE", "EMPLEADO", "DÍA", "HORA INICIO", "HORA FIN", "HORAS", "TIPO DE TURNO"};
            for (int i = 0; i < headers.length; i++) {
                createCell(h, i, headers[i], header);
            }

            // Filtro: solo turnos activos y no eliminados
            List<Shift> sortedShifts = shifts.stream()
                .filter(s -> Boolean.TRUE.equals(s.getStatus()) && s.getDeletedAt() == null)
                .sorted((s1, s2) -> {
                    String loc1 = s1.getLocation() != null ? s1.getLocation().getName() : "";
                    String loc2 = s2.getLocation() != null ? s2.getLocation().getName() : "";
                    int locCompare = loc1.compareTo(loc2);
                    if (locCompare != 0) return locCompare;

                    String emp1 = s1.getEmployee() != null
                            ? s1.getEmployee().getLastName() + s1.getEmployee().getFirstName() : "";
                    String emp2 = s2.getEmployee() != null
                            ? s2.getEmployee().getLastName() + s2.getEmployee().getFirstName() : "";
                    int empCompare = emp1.compareTo(emp2);
                    if (empCompare != 0) return empCompare;

                    return s1.getDate().compareTo(s2.getDate());
                })
                .collect(Collectors.toList());

            String currentLocation = "";
            String currentEmployee = "";
            int dataRow = 0;

            for (int i = 0; i < sortedShifts.size(); i++) {

                Shift shift = sortedShifts.get(i);

                String locationName = shift.getLocation() != null ? shift.getLocation().getName() : "";
                String empName = "";
                if (shift.getEmployee() != null) {
                    String firstName = shift.getEmployee().getFirstName() != null ? shift.getEmployee().getFirstName() : "";
                    String lastName  = shift.getEmployee().getLastName()  != null ? shift.getEmployee().getLastName()  : "";
                    empName = (firstName + " " + lastName).trim();
                }

                // ✅ CORRECCIÓN: primero insertar encabezado de sede,
                //    luego crear la fila de datos — siempre en orden
                if (!locationName.equals(currentLocation)) {
                    currentLocation = locationName;
                    currentEmployee = "";
                    dataRow = 0;

                    // Fila de encabezado de sede
                    Row locRow = sheet.createRow(row++);
                    createCell(locRow, 0, "📍 " + locationName.toUpperCase(), locationHdr);
                    sheet.addMergedRegion(new CellRangeAddress(
                            locRow.getRowNum(), locRow.getRowNum(), 0, 6));
                }

                // Fila de datos — siempre se crea después del encabezado
                CellStyle st = (dataRow % 2 == 0) ? even : odd;
                Row rw = sheet.createRow(row++);

                createCell(rw, 0, "", st);

                if (!empName.equals(currentEmployee)) {
                    currentEmployee = empName;
                }
                createCell(rw, 1, empName, st);

                int dayOfMonth = shift.getDate() != null ? shift.getDate().getDayOfMonth() : 0;
                createCellInt(rw, 2, dayOfMonth, st);

                String horaInicio = "";
                if (shift.getShiftType() != null && shift.getShiftType().getStartTime() != null) {
                    horaInicio = shift.getShiftType().getStartTime().toString();
                    if (horaInicio.length() > 5) horaInicio = horaInicio.substring(0, 5);
                }
                createCell(rw, 3, horaInicio, st);

                String horaFin = "";
                if (shift.getShiftType() != null && shift.getShiftType().getEndTime() != null) {
                    horaFin = shift.getShiftType().getEndTime().toString();
                    if (horaFin.length() > 5) horaFin = horaFin.substring(0, 5);
                }
                createCell(rw, 4, horaFin, st);

                double horasTurno = 0.0;
                if (shift.getShiftType() != null
                        && shift.getShiftType().getStartTime() != null
                        && shift.getShiftType().getEndTime() != null) {

                    LocalTime start = shift.getShiftType().getStartTime();
                    LocalTime end   = shift.getShiftType().getEndTime();
                    long minutos    = Duration.between(start, end).toMinutes();
                    if (minutos < 0) minutos += 24 * 60;
                    horasTurno = minutos / 60.0;
                }
                createCell(rw, 5, horasTurno, st);

                createCell(rw, 6, shift.getShiftType() != null ? shift.getShiftType().getName() : "", st);

                dataRow++;
            }

            // Fila total
            Row totalRow = sheet.createRow(row);
            createCell(totalRow, 0, "TOTAL TURNOS:", locationHdr);
            createCellInt(totalRow, 1, sortedShifts.size(), locationHdr);
            sheet.addMergedRegion(new CellRangeAddress(row, row, 0, 1));

            autoSizeAllSheets(workbook);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =====================================================
    // ESTILOS
    // =====================================================
    private CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 18);
        f.setColor(IndexedColors.RED.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle subTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = base(wb);
        s.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle grayHeaderStyle(Workbook wb) {
        CellStyle s = base(wb);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.BLACK.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle dataStyle(Workbook wb, boolean even) {
        CellStyle s = base(wb);
        s.setFillForegroundColor(
                even ? IndexedColors.WHITE.getIndex()
                     : IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle redSoftStyle(Workbook wb) {
        CellStyle s = base(wb);
        s.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.BLACK.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle locationHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle base(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    // =====================================================
    // FORMATO CONDICIONAL
    // =====================================================
    private void applyConditional(Sheet sheet, int start, int end, int col) {
        SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule =
                scf.createConditionalFormattingRule(ComparisonOperator.GT, "0");
        PatternFormatting fill = rule.createPatternFormatting();
        fill.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        CellRangeAddress[] range = {new CellRangeAddress(start, end, col, col)};
        scf.addConditionalFormatting(range, rule);
    }

    // =====================================================
    // AUTO AJUSTE
    // =====================================================
    private void autoSizeAllSheets(Workbook wb) {
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            int maxCol = getMaxColumns(sheet);
            for (int col = 0; col < maxCol; col++) {
                sheet.autoSizeColumn(col);
                int width = sheet.getColumnWidth(col) + 1200;
                if (width > 15000) width = 15000;
                if (width < 3500)  width = 3500;
                sheet.setColumnWidth(col, width);
            }
        }
    }

    private int getMaxColumns(Sheet sheet) {
        int max = 0;
        for (Row row : sheet) {
            if (row.getLastCellNum() > max) max = row.getLastCellNum();
        }
        return max;
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private void configSheet(Sheet s) {
        s.setMargin(Sheet.LeftMargin, 0.5);
        s.setMargin(Sheet.RightMargin, 0.5);
        s.setMargin(Sheet.TopMargin, 0.75);
        s.setMargin(Sheet.BottomMargin, 0.75);
        s.setDefaultRowHeightInPoints(24);
    }

    private void createCell(Row row, int col, String val, CellStyle st) {
        Cell c = row.createCell(col);
        c.setCellValue(val == null ? "" : val);
        c.setCellStyle(st);
    }

    private void createCell(Row row, int col, Number val, CellStyle st) {
        Cell c = row.createCell(col);
        c.setCellValue(val == null ? 0 : val.doubleValue());
        Workbook wb = row.getSheet().getWorkbook();
        DataFormat format = wb.createDataFormat();
        CellStyle nuevo = wb.createCellStyle();
        nuevo.cloneStyleFrom(st);
        nuevo.setDataFormat(format.getFormat("0.00"));
        c.setCellStyle(nuevo);
    }

    private void createCellInt(Row row, int col, Number val, CellStyle st) {
        Cell c = row.createCell(col);
        c.setCellValue(val == null ? 0 : val.doubleValue());
        Workbook wb = row.getSheet().getWorkbook();
        DataFormat format = wb.createDataFormat();
        CellStyle nuevo = wb.createCellStyle();
        nuevo.cloneStyleFrom(st);
        nuevo.setDataFormat(format.getFormat("0"));
        c.setCellStyle(nuevo);
    }

    private double nvl(Number n) {
        return n == null ? 0 : n.doubleValue();
    }

    private String getMonth(int m) {
        return Month.of(m)
                .getDisplayName(TextStyle.FULL, LOCALE_ES)
                .toUpperCase();
    }
}