package MicrofarmaHorarios.Schedules.DTO.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShiftChangeRequestDto {

    @NotNull(message = "ID del turno original es obligatorio")
    private String originalShiftId;

    private LocalDate requestedDate;

    private String requestedShiftTypeId;

    private String requestedLocationId;

    @NotBlank(message = "La razón es obligatoria")
    private String reason;

    public ShiftChangeRequestDto() {}

    public String getOriginalShiftId() {
        return originalShiftId;
    }

    public void setOriginalShiftId(String originalShiftId) {
        this.originalShiftId = originalShiftId;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getRequestedShiftTypeId() {
        return requestedShiftTypeId;
    }

    public void setRequestedShiftTypeId(String requestedShiftTypeId) {
        this.requestedShiftTypeId = requestedShiftTypeId;
    }

    public String getRequestedLocationId() {
        return requestedLocationId;
    }

    public void setRequestedLocationId(String requestedLocationId) {
        this.requestedLocationId = requestedLocationId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}