package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeRepository;
import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationLocationRepository;
import MicrofarmaHorarios.Schedules.DTO.Request.PdfScheduleDataDto;
import MicrofarmaHorarios.Schedules.DTO.Request.PdfScheduleDataDto.PdfShiftData;

/**
 * Service for validating PDF schedule import data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleImportValidationService {

    private final IHumanResourcesEmployeeRepository employeeRepository;
    private final IOrganizationLocationRepository locationRepository;
    
    /**
     * Result of validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<ValidationWarning> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<ValidationWarning> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<ValidationWarning> getWarnings() {
            return warnings;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, new ArrayList<>(), new ArrayList<>());
        }
        
        public static ValidationResult withWarnings(List<ValidationWarning> warnings) {
            return new ValidationResult(true, new ArrayList<>(), warnings);
        }
        
        public static ValidationResult error(String message) {
            List<String> errors = new ArrayList<>();
            errors.add(message);
            return new ValidationResult(false, errors, new ArrayList<>());
        }
        
        public static ValidationResult errors(List<String> errors) {
            return new ValidationResult(false, errors, new ArrayList<>());
        }
    }
    
    /**
     * Warning during validation.
     */
    public static class ValidationWarning {
        private final String code;
        private final String message;
        private final LocalDate affectedDate;
        
        public ValidationWarning(String code, String message, LocalDate affectedDate) {
            this.code = code;
            this.message = message;
            this.affectedDate = affectedDate;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public LocalDate getAffectedDate() {
            return affectedDate;
        }
    }
    
    /**
     * Validate the parsed PDF data before importing.
     * 
     * @param data The parsed PDF data
     * @param providedLocationId Optional location ID provided by user
     * @return Validation result
     */
    public ValidationResult validate(PdfScheduleDataDto data, String providedLocationId) {
        log.info("Validating PDF schedule import data");
        
        List<String> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();
        
        // Validate employee
        if (data.getEmployeeCode() != null && !data.getEmployeeCode().isEmpty()) {
            Employee employee = employeeRepository.findById(data.getEmployeeCode()).orElse(null);
            if (employee == null) {
                // Try to find by email or other identifier
                warnings.add(new ValidationWarning(
                        "EMPLOYEE_NOT_FOUND",
                        "Employee with code " + data.getEmployeeCode() + " not found in database",
                        null
                ));
            }
        }
        
        // Validate location
        Location location = null;
        if (providedLocationId != null && !providedLocationId.isEmpty()) {
            location = locationRepository.findById(providedLocationId).orElse(null);
            if (location == null) {
                errors.add("Location with ID " + providedLocationId + " not found");
            }
        } else if (data.getLocationName() != null && !data.getLocationName().isEmpty()) {
            // Try to find location by name
            location = locationRepository.findById(data.getLocationName()).orElse(null);
            if (location == null) {
                warnings.add(new ValidationWarning(
                        "LOCATION_NOT_FOUND",
                        "Location '" + data.getLocationName() + "' not found, will use default",
                        null
                ));
            }
        }
        
        // Validate month/year
        if (data.getMonth() <= 0 || data.getMonth() > 12) {
            errors.add("Invalid month: " + data.getMonth());
        }
        
        if (data.getYear() < 2000 || data.getYear() > 2100) {
            errors.add("Invalid year: " + data.getYear());
        }
        
        // Validate shifts
        if (data.getShifts() == null || data.getShifts().isEmpty()) {
            warnings.add(new ValidationWarning(
                    "NO_SHIFTS",
                    "No shifts found in the PDF",
                    null
            ));
        } else {
            // Validate each shift date
            for (PdfShiftData shift : data.getShifts()) {
                if (shift.getDate() == null) {
                    errors.add("Shift with missing date found");
                    continue;
                }
                
                // Check if date is in the expected month/year
                if (shift.getDate().getMonthValue() != data.getMonth() ||
                    shift.getDate().getYear() != data.getYear()) {
                    warnings.add(new ValidationWarning(
                            "DATE_MISMATCH",
                            "Shift date " + shift.getDate() + " doesn't match expected month/year",
                            shift.getDate()
                    ));
                }
                
                // Check for multiple shifts on same date (report as warning, not error)
                // Multiple shifts per date is normal (different shift types: MAÑANA, TARDE, NOCHE)
                long sameDateCount = data.getShifts().stream()
                        .filter(s -> s.getDate() != null && s.getDate().equals(shift.getDate()))
                        .count();
                if (sameDateCount > 1) {
                    warnings.add(new ValidationWarning(
                            "MULTIPLE_SHIFTS_SAME_DATE",
                            "Multiple shifts found for date " + shift.getDate() + " - this is normal for different shift types",
                            shift.getDate()
                    ));
                }
            }
        }
        
        log.info("Validation completed: {} errors, {} warnings", errors.size(), warnings.size());
        
        if (!errors.isEmpty()) {
            return ValidationResult.errors(errors);
        } else if (!warnings.isEmpty()) {
            return ValidationResult.withWarnings(warnings);
        } else {
            return ValidationResult.success();
        }
    }
    
    /**
     * Check if an employee exists in the database.
     */
    public boolean employeeExists(String employeeCode) {
        if (employeeCode == null || employeeCode.isEmpty()) {
            return false;
        }
        return employeeRepository.findById(employeeCode).isPresent();
    }
    
    /**
     * Check if a location exists in the database.
     */
    public boolean locationExists(String locationId) {
        if (locationId == null || locationId.isEmpty()) {
            return false;
        }
        return locationRepository.findById(locationId).isPresent();
    }
}
