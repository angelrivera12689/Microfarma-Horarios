package MicrofarmaHorarios.Schedules.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShiftChangeDecisionDto {

    @NotNull(message = "La decisión es obligatoria")
    private Boolean approved;

    @NotBlank(message = "La decisión del admin es obligatoria")
    private String adminDecision;

    public ShiftChangeDecisionDto() {}

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getAdminDecision() {
        return adminDecision;
    }

    public void setAdminDecision(String adminDecision) {
        this.adminDecision = adminDecision;
    }

}