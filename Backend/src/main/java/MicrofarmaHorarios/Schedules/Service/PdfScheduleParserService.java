package MicrofarmaHorarios.Schedules.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import lombok.extern.slf4j.Slf4j;
import MicrofarmaHorarios.Schedules.DTO.Request.PdfScheduleDataDto;
import MicrofarmaHorarios.Schedules.DTO.Request.PdfScheduleDataDto.PdfShiftData;

/**
 * Service for parsing PDF schedule files and extracting shift data.
 */
@Service
@Slf4j
public class PdfScheduleParserService {

    private static final Pattern EMPLOYEE_PATTERN = Pattern.compile(
            "(?:EMPLEADO|EMPLOYEE)[\\s:]*([A-Za-zÁÉÍÓÚáéíóú\\s]+)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern CODE_PATTERN = Pattern.compile(
            "(?:Código|Code|Código de Empleado)[\\s:]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "(?:Sede|Location|Sede de Trabajo)[\\s:]*([A-Za-zÁÉÍÓÚ0-9\\s]+)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern MONTH_YEAR_PATTERN = Pattern.compile(
            "(?:Mes|Año|Month|Year|Mes/Año)[\\s:]*([A-Za-z]+)[\\s/]+(\\d{4})",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SHIFT_PATTERN = Pattern.compile(
            "(?:(?:LUNES|MARTS?|MIÉRCOLES?|JUEVES?|VIERNES?|SÁBADO?|DOMINGO?|LUN|MAR|MIE|JUE|VIE|SAB|DOM)[A-Za-z]*\\s*)?"
            + "(\\d{1,2})[\\s/-]*(\\d{1,2})?[\\s/-]*(\\d{4})?"            // Date: DD/MM/YYYY or similar
            + "[\\s,]+(\\d{1,2}:\\d{2})\\s*[-–→to]+\\s*(\\d{1,2}:\\d{2})" // Time range
            + "[\\s,]*(.*?)$",                                           // Shift type
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern REST_DAY_PATTERN = Pattern.compile(
            "(?:LIBRE|REST|Descanso|DÍA LIBRE|OFF)",
            Pattern.CASE_INSENSITIVE
    );
    
    // Pattern for employee shifts: "EmployeeName: time-time" or "EmployeeName: time-time / time-time"
    private static final Pattern EMPLOYEE_SHIFT_PATTERN = Pattern.compile(
            "^([A-Za-zÁÉÍÓÚáéíóú\\s]+?)\\s*:\\s*(\\d{1,2}(?:am|pm)(?:-\\d{1,2}(?:am|pm))?(?:\\s*/\\s*\\d{1,2}(?:am|pm)(?:-\\d{1,2}(?:am|pm))?)?)",
            Pattern.CASE_INSENSITIVE
    );
    
    // Pattern for day numbers row: "1" or "2 3 4 5 6 7 8"
    private static final Pattern DAY_NUMBERS_PATTERN = Pattern.compile(
            "^(\\d{1,2})(?:\\s+(\\d{1,2}))?(?:\\s+(\\d{1,2}))?(?:\\s+(\\d{1,2}))?(?:\\s+(\\d{1,2}))?(?:\\s+(\\d{1,2}))?(?:\\s+(\\d{1,2}))?$"
    );
    
    // Pattern for location name (all caps words)
    private static final Pattern LOCATION_HEADER_PATTERN = Pattern.compile(
            "^[A-ZÁÉÍÓÚÁÉÍÓÚ\\s]+$"
    );
    
    private static final Map<String, Integer> MONTH_MAP = new HashMap<>();
    
    static {
        MONTH_MAP.put("ENERO", 1);
        MONTH_MAP.put("FEBRERO", 2);
        MONTH_MAP.put("MARZO", 3);
        MONTH_MAP.put("ABRIL", 4);
        MONTH_MAP.put("MAYO", 5);
        MONTH_MAP.put("JUNIO", 6);
        MONTH_MAP.put("JULIO", 7);
        MONTH_MAP.put("AGOSTO", 8);
        MONTH_MAP.put("SEPTIEMBRE", 9);
        MONTH_MAP.put("OCTUBRE", 10);
        MONTH_MAP.put("NOVIEMBRE", 11);
        MONTH_MAP.put("DICIEMBRE", 12);
    }
    
    private static final Map<String, String> DAY_NAME_MAP = new HashMap<>();
    
    static {
        DAY_NAME_MAP.put("LUNES", "LUNES");
        DAY_NAME_MAP.put("LUN", "LUNES");
        DAY_NAME_MAP.put("MARTES", "MARTES");
        DAY_NAME_MAP.put("MAR", "MARTES");
        DAY_NAME_MAP.put("MIÉRCOLES", "MIÉRCOLES");
        DAY_NAME_MAP.put("MIE", "MIÉRCOLES");
        DAY_NAME_MAP.put("JUEVES", "JUEVES");
        DAY_NAME_MAP.put("JUE", "JUEVES");
        DAY_NAME_MAP.put("VIERNES", "VIERNES");
        DAY_NAME_MAP.put("VIE", "VIERNES");
        DAY_NAME_MAP.put("SÁBADO", "SÁBADO");
        DAY_NAME_MAP.put("SAB", "SÁBADO");
        DAY_NAME_MAP.put("DOMINGO", "DOMINGO");
        DAY_NAME_MAP.put("DOM", "DOMINGO");
    }
    
    /**
     * Parse a PDF file and extract schedule data.
     * 
     * @param file The PDF file to parse
     * @return PdfScheduleDataDto containing extracted data
     * @throws IOException If the file cannot be read
     */
    public PdfScheduleDataDto parsePdf(MultipartFile file) throws IOException {
        log.info("Parsing PDF file: {}", file.getOriginalFilename());
        
        String extractedText = extractTextFromPdf(file);
        log.info("DEBUG: Extracted text length: {} characters", extractedText.length());
        log.info("DEBUG: Full extracted text:\n{}", extractedText);
        
        PdfScheduleDataDto data = new PdfScheduleDataDto();
        
        // Extract metadata
        extractEmployeeInfo(extractedText, data);
        log.info("DEBUG: Employee info - name: {}, code: {}", data.getEmployeeName(), data.getEmployeeCode());
        
        extractLocation(extractedText, data);
        log.info("DEBUG: Location: {}", data.getLocationName());
        
        extractMonthYear(extractedText, data);
        log.info("DEBUG: Month/Year: {}/{}", data.getMonth(), data.getYear());
        
        // Extract shifts
        List<PdfShiftData> shifts = extractShifts(extractedText, data.getYear(), data.getMonth());
        data.setShifts(shifts);
        
        log.info("Parsed {} shifts from PDF", shifts.size());
        
        return data;
    }
    
    /**
     * Extract text content from a PDF file.
     */
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        StringBuilder text = new StringBuilder();
        
        try (PdfReader reader = new PdfReader(file.getInputStream());
             PdfDocument pdfDoc = new PdfDocument(reader)) {
            
            int numberOfPages = pdfDoc.getNumberOfPages();
            
            for (int i = 1; i <= numberOfPages; i++) {
                try {
                    String pageText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                    if (pageText != null && !pageText.isEmpty()) {
                        text.append(pageText).append("\n");
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract text from page {}: {}", i, e.getMessage());
                }
            }
        }
        
        return text.toString();
    }
    
    /**
     * Extract employee information from the text.
     */
    private void extractEmployeeInfo(String text, PdfScheduleDataDto data) {
        Matcher employeeMatcher = EMPLOYEE_PATTERN.matcher(text);
        if (employeeMatcher.find()) {
            String name = employeeMatcher.group(1).trim();
            data.setEmployeeName(name);
            log.debug("Found employee name: {}", name);
        }
        
        Matcher codeMatcher = CODE_PATTERN.matcher(text);
        if (codeMatcher.find()) {
            String code = codeMatcher.group(1).trim();
            data.setEmployeeCode(code);
            log.debug("Found employee code: {}", code);
        }
    }
    
    /**
     * Extract location information from the text.
     */
    private void extractLocation(String text, PdfScheduleDataDto data) {
        Matcher locationMatcher = LOCATION_PATTERN.matcher(text);
        if (locationMatcher.find()) {
            String location = locationMatcher.group(1).trim();
            data.setLocationName(location);
            log.debug("Found location: {}", location);
        }
    }
    
    /**
     * Extract month and year from the text.
     */
    private void extractMonthYear(String text, PdfScheduleDataDto data) {
        Matcher matcher = MONTH_YEAR_PATTERN.matcher(text);
        if (matcher.find()) {
            String monthName = matcher.group(1).trim().toUpperCase();
            String yearStr = matcher.group(2);
            
            Integer month = MONTH_MAP.get(monthName);
            if (month != null) {
                data.setMonth(month);
                data.setYear(Integer.parseInt(yearStr));
                log.debug("Found month/year: {}/{}", month, yearStr);
            }
        }
    }
    
    /**
     * Extract shift entries from the text using calendar layout parsing.
     * This method handles the weekly calendar format where:
     * - Days of week are in columns
     * - Day numbers appear in header rows
     * - Employee shifts are listed under each location
     */
    private List<PdfShiftData> extractShifts(String text, int defaultYear, int defaultMonth) {
        List<PdfShiftData> shifts = new ArrayList<>();
        
        String[] lines = text.split("\\r?\\n");
        log.info("DEBUG: Processing {} lines from text", lines.length);
        
        // Track the calendar layout state
        int[] dayNumbers = new int[7]; // Array to hold day numbers for each day of week (Mon-Sun)
        boolean[] hasDayNumber = new boolean[7]; // Track which columns have valid day numbers
        int currentWeekStartDay = 1; // Default start day for first week
        int maxDaySeen = 0;
        
        // Pattern to match day names row
        Pattern dayNamesPattern = Pattern.compile(
            "^(?:LUNES|MARTES|MIERCOLES|JUEVES|VIERNES|SÁBADO|DOMINGO|LUN|MAR|MIE|JUE|VIE|SAB|DOM)",
            Pattern.CASE_INSENSITIVE
        );
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            log.info("DEBUG: Line [{}]: '{}'", i, line);
            
            // Check if this line contains day names (columns header)
            if (dayNamesPattern.matcher(line).find()) {
                log.info("DEBUG: Found day names row");
                continue;
            }
            
            // Check if this is a day numbers row (e.g., "1" or "2 3 4 5 6 7 8")
            Matcher dayNumbersMatcher = DAY_NUMBERS_PATTERN.matcher(line);
            if (dayNumbersMatcher.matches()) {
                log.info("DEBUG: Found day numbers row");
                
                // Reset day tracking
                hasDayNumber = new boolean[7];
                
                // Extract day numbers for each column
                for (int col = 0; col < 7; col++) {
                    String dayStr = dayNumbersMatcher.group(col + 1);
                    if (dayStr != null) {
                        int dayNum = Integer.parseInt(dayStr);
                        dayNumbers[col] = dayNum;
                        hasDayNumber[col] = true;
                        
                        if (dayNum > maxDaySeen) {
                            maxDaySeen = dayNum;
                        }
                        
                        // If we see day 1 after seeing higher numbers, it's a new month
                        if (dayNum == 1 && maxDaySeen > 28) {
                            // New month started, reset
                            maxDaySeen = dayNum;
                        }
                    }
                }
                
                log.info("DEBUG: Day numbers parsed: {}", java.util.Arrays.toString(dayNumbers));
                continue;
            }
            
            // Check if this is a location header (all caps)
            Matcher locationMatcher = LOCATION_HEADER_PATTERN.matcher(line);
            if (locationMatcher.find() && !line.matches("^\\d+.*") && line.length() > 3) {
                log.info("DEBUG: Found location header: {}", line);
                continue;
            }
            
            // Try to parse employee shift: "EmployeeName: time-time" or "EmployeeName: time-time / time-time"
            Matcher shiftMatcher = EMPLOYEE_SHIFT_PATTERN.matcher(line);
            if (shiftMatcher.find()) {
                String employeeName = shiftMatcher.group(1).trim();
                String timeInfo = shiftMatcher.group(2).trim();
                
                log.info("DEBUG: Found employee shift - Name: '{}', Time: '{}'", employeeName, timeInfo);
                
                // Parse time info (may contain multiple shifts separated by "/")
                String[] timeParts = timeInfo.split("\\s*/\\s*");
                
                for (int col = 0; col < 7; col++) {
                    if (hasDayNumber[col]) {
                        int day = dayNumbers[col];
                        
                        for (String timePart : timeParts) {
                            PdfShiftData shift = parseTimePart(timePart.trim(), day, defaultYear, defaultMonth, employeeName);
                            if (shift != null) {
                                shifts.add(shift);
                                log.info("DEBUG: Added shift - Date: {}, Employee: {}, Time: {}-{}", 
                                    shift.getDate(), employeeName, shift.getStartTime(), shift.getEndTime());
                            }
                        }
                    }
                }
                continue;
            }
            
            // Try original shift pattern as fallback
            PdfShiftData shift = parseShiftLine(line, defaultYear, defaultMonth);
            if (shift != null) {
                log.info("DEBUG: Successfully parsed shift from line [{}]: date={}, startTime={}, endTime={}, type={}", 
                    i, shift.getDate(), shift.getStartTime(), shift.getEndTime(), shift.getShiftTypeName());
                shifts.add(shift);
            } else {
                log.info("DEBUG: Line [{}] did not match any shift pattern", i);
            }
        }
        
        log.info("DEBUG: Total shifts extracted: {}", shifts.size());
        return shifts;
    }
    
    /**
     * Parse a time part like "7am-2pm" or "10pm-7am" into a shift.
     */
    private PdfShiftData parseTimePart(String timePart, int day, int year, int month, String employeeName) {
        try {
            // Match patterns like "7am-2pm" or "10pm-7am" or "7am-3pm"
            Pattern timeRangePattern = Pattern.compile(
                "(\\d{1,2})(am|pm)\\s*[-–]\\s*(\\d{1,2})(am|pm)",
                Pattern.CASE_INSENSITIVE
            );
            
            Matcher matcher = timeRangePattern.matcher(timePart);
            if (!matcher.find()) {
                log.debug("DEBUG: Time part '{}' did not match time range pattern", timePart);
                return null;
            }
            
            int startHour = Integer.parseInt(matcher.group(1));
            String startAmPm = matcher.group(2).toLowerCase();
            int endHour = Integer.parseInt(matcher.group(3));
            String endAmPm = matcher.group(4).toLowerCase();
            
            // Convert to 24-hour format
            if (startAmPm.equals("pm") && startHour != 12) {
                startHour += 12;
            } else if (startAmPm.equals("am") && startHour == 12) {
                startHour = 0;
            }
            
            if (endAmPm.equals("pm") && endHour != 12) {
                endHour += 12;
            } else if (endAmPm.equals("am") && endHour == 12) {
                endHour = 0;
            }
            
            PdfShiftData shift = new PdfShiftData();
            shift.setRestDay(false);
            shift.setDate(LocalDate.of(year, month, day));
            shift.setStartTime(LocalTime.of(startHour, 0));
            shift.setEndTime(LocalTime.of(endHour, 0));
            shift.setShiftTypeName(determineShiftType(shift.getStartTime(), shift.getEndTime()));
            
            log.debug("DEBUG: Parsed time '{}' -> Start: {}:00, End: {}:00", 
                timePart, startHour, endHour);
            
            return shift;
        } catch (Exception e) {
            log.warn("DEBUG: Failed to parse time part '{}': {}", timePart, e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse a single line to extract shift data.
     */
    private PdfShiftData parseShiftLine(String line, int defaultYear, int defaultMonth) {
        line = line.trim();
        if (line.isEmpty()) {
            log.debug("DEBUG: Skipping empty line");
            return null;
        }
        
        log.debug("DEBUG: Trying to parse line: '{}'", line);
        
        if (REST_DAY_PATTERN.matcher(line).find()) {
            log.debug("DEBUG: Line matches REST_DAY_PATTERN");
            PdfShiftData restDay = new PdfShiftData();
            restDay.setRestDay(true);
            restDay.setShiftTypeName("LIBRE");
            
            Matcher dateMatcher = Pattern.compile("(\\d{1,2})[\\s/-]*(\\d{1,2})?[\\s/-]*(\\d{4})?").matcher(line);
            if (dateMatcher.find()) {
                int day = Integer.parseInt(dateMatcher.group(1));
                int month = defaultMonth;
                int year = defaultYear;
                
                if (dateMatcher.group(2) != null) {
                    month = Integer.parseInt(dateMatcher.group(2));
                }
                if (dateMatcher.group(3) != null) {
                    year = Integer.parseInt(dateMatcher.group(3));
                }
                
                restDay.setDate(LocalDate.of(year, month, day));
                log.debug("DEBUG: Rest day parsed: {}", restDay.getDate());
            }
            
            return restDay;
        }
        
        Matcher shiftMatcher = SHIFT_PATTERN.matcher(line);
        boolean matches = shiftMatcher.find();
        log.debug("DEBUG: SHIFT_PATTERN match result: {}", matches);
        
        if (matches) {
            try {
                String dayName = shiftMatcher.group(1);
                String dayStr = shiftMatcher.group(2);
                String monthStr = shiftMatcher.group(3);
                String yearStr = shiftMatcher.group(4);
                String startTimeStr = shiftMatcher.group(5);
                String endTimeStr = shiftMatcher.group(6);
                String shiftType = shiftMatcher.group(7);
                
                log.debug("DEBUG: Groups - dayName: {}, dayStr: {}, monthStr: {}, yearStr: {}, startTime: {}, endTime: {}, shiftType: {}",
                    dayName, dayStr, monthStr, yearStr, startTimeStr, endTimeStr, shiftType);
                
                if (dayStr == null || startTimeStr == null) {
                    log.debug("DEBUG: Missing dayStr or startTimeStr, returning null");
                    return null;
                }
                
                PdfShiftData shift = new PdfShiftData();
                shift.setRestDay(false);
                
                int day = Integer.parseInt(dayStr);
                int month = monthStr != null ? Integer.parseInt(monthStr) : defaultMonth;
                int year = yearStr != null ? Integer.parseInt(yearStr) : defaultYear;
                shift.setDate(LocalDate.of(year, month, day));
                
                if (dayName != null) {
                    String normalizedDay = dayName.trim().toUpperCase();
                    for (Map.Entry<String, String> entry : DAY_NAME_MAP.entrySet()) {
                        if (normalizedDay.startsWith(entry.getKey())) {
                            shift.setDayName(entry.getValue());
                            break;
                        }
                    }
                    if (shift.getDayName() == null) {
                        shift.setDayName(dayName.trim());
                    }
                }
                
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
                shift.setStartTime(LocalTime.parse(startTimeStr, timeFormatter));
                shift.setEndTime(LocalTime.parse(endTimeStr, timeFormatter));
                
                if (shiftType != null && !shiftType.trim().isEmpty()) {
                    shift.setShiftTypeName(shiftType.trim());
                } else {
                    shift.setShiftTypeName(determineShiftType(shift.getStartTime(), shift.getEndTime()));
                }
                
                log.debug("DEBUG: Successfully parsed shift: date={}, start={}, end={}, type={}",
                    shift.getDate(), shift.getStartTime(), shift.getEndTime(), shift.getShiftTypeName());
                
                return shift;
            } catch (Exception e) {
                log.warn("DEBUG: Failed to parse shift line '{}': {}", line, e.getMessage());
                return null;
            }
        }
        
        log.debug("DEBUG: Line did not match any pattern, returning null");
        return null;
    }
    
    /**
     * Determine shift type based on time range.
     */
    private String determineShiftType(LocalTime start, LocalTime end) {
        int startHour = start.getHour();
        
        if (startHour >= 6 && startHour < 14) {
            return "MAÑANA";
        } else if (startHour >= 14 && startHour < 22) {
            return "TARDE";
        } else if (startHour >= 22 || startHour < 6) {
            return "NOCHE";
        }
        
        return "TURNO";
    }
}
