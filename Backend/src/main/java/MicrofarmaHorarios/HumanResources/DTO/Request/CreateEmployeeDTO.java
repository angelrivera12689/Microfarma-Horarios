package MicrofarmaHorarios.HumanResources.DTO.Request;

import jakarta.validation.constraints.*;

/**
 * DTO para validar creación de empleados
 * ✅ Evita inyección SQL y valida datos
 */
public class CreateEmployeeDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Identification number is required")
    @Pattern(regexp = "\\d{6,12}", message = "Identification must be 6-12 digits")
    private String identificationNumber;

    @NotNull(message = "Position ID is required")
    @Min(value = 1, message = "Position ID must be greater than 0")
    private Long positionId;

    @NotNull(message = "Contract type ID is required")
    @Min(value = 1, message = "Contract type ID must be greater than 0")
    private Long contractTypeId;

    @NotNull(message = "Location ID is required")
    @Min(value = 1, message = "Location ID must be greater than 0")
    private Long locationId;

    @Min(value = 0, message = "Salary cannot be negative")
    private Double salary;

    public CreateEmployeeDTO() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getContractTypeId() {
        return contractTypeId;
    }

    public void setContractTypeId(Long contractTypeId) {
        this.contractTypeId = contractTypeId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
}
