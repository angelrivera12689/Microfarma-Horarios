package MicrofarmaHorarios.Schedules.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeRepository;
import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationLocationRepository;
import MicrofarmaHorarios.Schedules.DTO.Request.PdfScheduleDataDto;
import MicrofarmaHorarios.Schedules.DTO.Request.PdfScheduleDataDto.PdfShiftData;
import MicrofarmaHorarios.Schedules.DTO.Response.PdfScheduleImportResultDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ShiftImportDetailDto;
import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftRepository;
import MicrofarmaHorarios.Schedules.Service.PdfScheduleParserService;
import MicrofarmaHorarios.Schedules.Service.ScheduleImportValidationService;
import MicrofarmaHorarios.Schedules.Service.ScheduleImportValidationService.ValidationResult;
import MicrofarmaHorarios.Schedules.Service.ShiftTypeDetectionService;

/**
 * REST Controller for importing schedules from PDF files.
 */
@RestController
@RequestMapping("/api/schedules/import")
@RequiredArgsConstructor
@Slf4j
public class SchedulesImportController {

    private final PdfScheduleParserService pdfParserService;
    private final ShiftTypeDetectionService shiftTypeDetectionService;
    private final ScheduleImportValidationService validationService;
    private final ISchedulesShiftRepository shiftRepository;
    private final IHumanResourcesEmployeeRepository employeeRepository;
    private final IOrganizationLocationRepository locationRepository;
    
    /**
     * Import schedule data from a PDF file.
     * 
     * @param file The PDF file to import
     * @param locationId Optional location ID to use
     * @param overwrite Whether to overwrite existing shifts
     * @return Result of the import operation
     */
    @PostMapping("/pdf")
    public ResponseEntity<PdfScheduleImportResultDto> importFromPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String locationId,
            @RequestParam(defaultValue = "false") Boolean overwrite) {
        
        log.info("Received PDF import request: file={}, locationId={}, overwrite={}", 
                file.getOriginalFilename(), locationId, overwrite);
        
        try {
            // Validate file type
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body(
                        PdfScheduleImportResultDto.error("El archivo debe ser un PDF", 
                                List.of("Solo se aceptan archivos PDF"))
                );
            }
            
            // Parse PDF
            PdfScheduleDataDto parsedData = pdfParserService.parsePdf(file);
            
            log.info("DEBUG: Parsed data summary:");
            log.info("DEBUG: Employee Name: {}", parsedData.getEmployeeName());
            log.info("DEBUG: Employee Code: {}", parsedData.getEmployeeCode());
            log.info("DEBUG: Location: {}", parsedData.getLocationName());
            log.info("DEBUG: Month/Year: {}/{}", parsedData.getMonth(), parsedData.getYear());
            log.info("DEBUG: Total shifts in parsed data: {}", 
                parsedData.getShifts() != null ? parsedData.getShifts().size() : 0);
            
            if (parsedData.getShifts() != null) {
                for (int i = 0; i < parsedData.getShifts().size(); i++) {
                    PdfShiftData shift = parsedData.getShifts().get(i);
                    log.info("DEBUG: Shift [{}]: date={}, restDay={}, startTime={}, endTime={}, type={}",
                        i, shift.getDate(), shift.isRestDay(), shift.getStartTime(), 
                        shift.getEndTime(), shift.getShiftTypeName());
                }
            }
            
            // Validate parsed data
            ValidationResult validation = validationService.validate(parsedData, locationId);
            if (!validation.isValid()) {
                return ResponseEntity.badRequest().body(
                        PdfScheduleImportResultDto.error("Error de validación", validation.getErrors())
                );
            }
            
            // Get location - required for saving shifts
            Location location = null;
            if (locationId != null && !locationId.isEmpty()) {
                location = locationRepository.findById(locationId).orElse(null);
            } else if (parsedData.getLocationName() != null && !parsedData.getLocationName().isEmpty()) {
                location = locationRepository.findById(parsedData.getLocationName()).orElse(null);
            }
            
            // If no location found, try to get the first available location as default
            if (location == null) {
                location = locationRepository.findAll().stream().findFirst().orElse(null);
            }
            
            // If still no location, return error
            if (location == null) {
                return ResponseEntity.badRequest().body(
                        PdfScheduleImportResultDto.error("Error de validación", 
                                List.of("No se encontró una ubicación para asignar a los turnos. Por favor especifique una ubicación."))
                );
            }
            
            // Get employee
            Employee employee = null;
            if (parsedData.getEmployeeCode() != null && !parsedData.getEmployeeCode().isEmpty()) {
                employee = employeeRepository.findById(parsedData.getEmployeeCode()).orElse(null);
            }
            
            // Import shifts
            log.info("DEBUG: Starting import of {} shifts", parsedData.getShifts() != null ? parsedData.getShifts().size() : 0);
            
            List<ShiftImportDetailDto> importedShifts = new ArrayList<>();
            int importedCount = 0;
            
            for (PdfShiftData shiftData : parsedData.getShifts()) {
                // Skip rest days
                if (shiftData.isRestDay()) {
                    continue;
                }
                
                // Check if shift already exists
                Optional<Shift> existingShift = shiftRepository
                        .findByEmployeeAndDate(employee, shiftData.getDate());
                
                if (existingShift.isPresent() && !overwrite) {
                    importedShifts.add(ShiftImportDetailDto.builder()
                            .id(existingShift.get().getId())
                            .date(shiftData.getDate())
                            .employeeName(employee != null ? employee.getFirstName() + " " + employee.getLastName() : null)
                            .locationName(location != null ? location.getName() : null)
                            .shiftTypeName(shiftData.getShiftTypeName())
                            .startTime(shiftData.getStartTime())
                            .endTime(shiftData.getEndTime())
                            .status("SKIPPED")
                            .build());
                    continue;
                }
                
                // Get or create shift type
                ShiftType shiftType = shiftTypeDetectionService.findOrCreateShiftType(
                        shiftData.getShiftTypeName(),
                        shiftData.getStartTime(),
                        shiftData.getEndTime()
                );
                
                // Create or update shift
                Shift shift = existingShift.orElseGet(Shift::new);
                shift.setDate(shiftData.getDate());
                shift.setEmployee(employee);
                shift.setLocation(location);
                shift.setShiftType(shiftType);
                shift.setNotes("Importado desde PDF: " + file.getOriginalFilename());
                
                Shift savedShift = shiftRepository.save(shift);
                
                importedShifts.add(ShiftImportDetailDto.builder()
                        .id(savedShift.getId())
                        .date(shiftData.getDate())
                        .employeeName(employee != null ? employee.getFirstName() + " " + employee.getLastName() : null)
                        .locationName(location != null ? location.getName() : null)
                        .shiftTypeName(shiftData.getShiftTypeName())
                        .startTime(shiftData.getStartTime())
                        .endTime(shiftData.getEndTime())
                        .status(existingShift.isPresent() ? "UPDATED" : "CREATED")
                        .build());
                
                importedCount++;
            }
            
            log.info("Successfully imported {} shifts from PDF", importedCount);
            
            return ResponseEntity.ok(PdfScheduleImportResultDto.success(importedCount, importedShifts));
            
        } catch (IOException e) {
            log.error("Error reading PDF file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    PdfScheduleImportResultDto.error("Error al procesar el PDF", 
                            List.of("Error al leer el archivo: " + e.getMessage()))
            );
        } catch (Exception e) {
            log.error("Error importing PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    PdfScheduleImportResultDto.error("Error al importar el PDF", 
                            List.of("Error inesperado: " + e.getMessage()))
            );
        }
    }
    
    /**
     * Get import template information.
     */
    @GetMapping("/template")
    public ResponseEntity<PdfScheduleImportResultDto> getTemplateInfo() {
        String templateInfo = """
                Formato esperado del PDF:
                
                El PDF debe contener la siguiente información:
                - Nombre del empleado
                - Código de empleado
                - Sede de trabajo
                - Mes y año del horario
                - Lista de turnos con fecha, hora de inicio y fin
                
                Patrones reconocidos:
                - Turnos de mañana: 08:00 - 16:00
                - Turnos de tarde: 14:00 - 22:00
                - Turnos de noche: 22:00 - 06:00
                - Días libre: LIBRE / REST
                """;
        
        return ResponseEntity.ok(PdfScheduleImportResultDto.builder()
                .success(true)
                .message(templateInfo)
                .totalShiftsImported(0)
                .totalErrors(0)
                .errors(null)
                .importedShifts(null)
                .build());
    }
}
