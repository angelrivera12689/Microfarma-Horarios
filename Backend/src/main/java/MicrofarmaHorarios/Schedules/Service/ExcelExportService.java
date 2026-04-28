package MicrofarmaHorarios.Schedules.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            int year) throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            createGlobalSheet(workbook, report.getGlobal(), month, year);
            createLocationsSheet(workbook, report.getLocations(), month, year);
            createEmployeesSheet(workbook, report.getEmployees(), month, year);
            createAlertsSheet(workbook, report.getEmployees(), month, year);

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
    private void createGlobalSheet(
            Workbook wb,
            GlobalReportDto g,
            int month,
            int year) {

        Sheet sheet = wb.createSheet("Resumen");
        configSheet(sheet);

        CellStyle title = titleStyle(wb);
        CellStyle sub = subTitleStyle(wb);
        CellStyle header = headerStyle(wb);
        CellStyle even = dataStyle(wb, true);
        CellStyle odd = dataStyle(wb, false);

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
                {"Total Empleados", g.getTotalEmployees(), "Personal activo"},
                {"Total Turnos", g.getTotalShifts(), "Turnos registrados"},
                {"Horas Totales", g.getTotalHours(), "Horas trabajadas"},
                {"Horas Extras", g.getTotalOvertimeHours(), "Horas adicionales"},
                {"Horas Regulares", g.getTotalRegularHours(), "Jornada normal"}
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
    private void createLocationsSheet(
            Workbook wb,
            List<LocationReportDto> list,
            int month,
            int year) {

        Sheet sheet = wb.createSheet("Ubicaciones");
        configSheet(sheet);

        CellStyle title = titleStyle(wb);
        CellStyle header = headerStyle(wb);
        CellStyle even = dataStyle(wb, true);
        CellStyle odd = dataStyle(wb, false);

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
            int year) {

        Sheet sheet = wb.createSheet("Empleados");
        configSheet(sheet);
        sheet.createFreezePane(0, 3);

        CellStyle title = titleStyle(wb);
        CellStyle header = headerStyle(wb);
        CellStyle even = dataStyle(wb, true);
        CellStyle odd = dataStyle(wb, false);
        CellStyle red = redSoftStyle(wb);

        Row r0 = sheet.createRow(0);
        createCell(r0, 0, "INFORME DE EMPLEADOS - " + getMonth(month) + " " + year, title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

        Row h = sheet.createRow(2);

        String[] headers = {
                "NOMBRE", "DÍAS", "TURNOS",
                "HORAS", "PROM DÍA", "PROM SEM",
                "EXTRAS >220", "REGULARES",
                "EXTRA D", "EXTRA N",
                "DOMINICAL", "FESTIVA"
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
            extra.setCellFormula("MAX(D" + (row + 1) + "-220,0)");
            extra.setCellStyle(nvl(e.getTotalHours()) > LIMITE_HORAS ? red : st);

            createCell(rw, 7, e.getRegularHours(), st);
            createCell(rw, 8, e.getDiurnaExtraHours(), st);
            createCell(rw, 9, e.getNocturnaExtraHours(), st);
            createCell(rw, 10, e.getDominicalHours(), st);
            createCell(rw, 11, e.getFestivoHours(), st);

            row++;
        }

        applyConditional(sheet, 3, row - 1, 6);
    }

    // =====================================================
    // ALERTAS
    // =====================================================
    private void createAlertsSheet(
            Workbook wb,
            List<EmployeeReportDto> all,
            int month,
            int year) {

        Sheet sheet = wb.createSheet("Alertas");
        configSheet(sheet);

        CellStyle title = titleStyle(wb);
        CellStyle header = grayHeaderStyle(wb);
        CellStyle normal = dataStyle(wb, true);
        CellStyle red = redSoftStyle(wb);

        List<EmployeeReportDto> list = all.stream()
                .filter(x -> nvl(x.getTotalHours()) > LIMITE_HORAS)
                .collect(Collectors.toList());

        Row r0 = sheet.createRow(0);
        createCell(r0, 0, "ALERTA EMPLEADOS +220 HORAS", title);
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
            c.setCellFormula("MAX(B" + (row + 1) + "-220,0)");
            c.setCellStyle(red);

            createCell(rw, 3, e.getDailyAvgHours(), normal);
            createCell(rw, 4, e.getWeeklyTotalHours(), normal);
            createCell(rw, 5, e.getTotalShifts(), normal);
            createCell(rw, 6, e.getWorkingDays(), normal);

            row++;
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
                     : IndexedColors.GREY_25_PERCENT.getIndex()
        );

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
    private void applyConditional(
            Sheet sheet,
            int start,
            int end,
            int col) {

        SheetConditionalFormatting scf =
                sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule =
                scf.createConditionalFormattingRule(
                        ComparisonOperator.GT, "0");

        PatternFormatting fill =
                rule.createPatternFormatting();

        fill.setFillForegroundColor(
                IndexedColors.CORAL.getIndex());

        fill.setFillPattern(
                PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] range = {
                new CellRangeAddress(start, end, col, col)
        };

        scf.addConditionalFormatting(range, rule);
    }

    // =====================================================
    // AUTO AJUSTE PERFECTO
    // =====================================================
    private void autoSizeAllSheets(Workbook wb) {

        for (int i = 0; i < wb.getNumberOfSheets(); i++) {

            Sheet sheet = wb.getSheetAt(i);
            int maxCol = getMaxColumns(sheet);

            for (int col = 0; col < maxCol; col++) {
                sheet.autoSizeColumn(col);

                int width = sheet.getColumnWidth(col);

                width += 1200; // espacio extra
                if (width > 15000) width = 15000;
                if (width < 3500) width = 3500;

                sheet.setColumnWidth(col, width);
            }
        }
    }

    private int getMaxColumns(Sheet sheet) {

        int max = 0;

        for (Row row : sheet) {
            if (row.getLastCellNum() > max) {
                max = row.getLastCellNum();
            }
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

        double numero = val == null ? 0 : val.doubleValue();
        c.setCellValue(numero);

        Workbook wb = row.getSheet().getWorkbook();

        DataFormat format = wb.createDataFormat();

        CellStyle nuevo = wb.createCellStyle();
        nuevo.cloneStyleFrom(st);
        nuevo.setDataFormat(format.getFormat("0.00"));

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