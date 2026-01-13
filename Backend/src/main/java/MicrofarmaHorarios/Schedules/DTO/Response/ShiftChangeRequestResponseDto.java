package MicrofarmaHorarios.Schedules.DTO.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import MicrofarmaHorarios.Schedules.Entity.RequestStatus;

public class ShiftChangeRequestResponseDto {

    private String id;
    private String employeeId;
    private String employeeName;
    private String originalShiftId;
    private LocalDate originalDate;
    private String originalShiftType;
    private String originalLocation;
    private LocalDate requestedDate;
    private String requestedShiftType;
    private String requestedLocation;
    private String reason;
    private RequestStatus status;
    private String adminDecision;
    private String approvedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShiftChangeRequestResponseDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getOriginalShiftId() {
        return originalShiftId;
    }

    public void setOriginalShiftId(String originalShiftId) {
        this.originalShiftId = originalShiftId;
    }

    public LocalDate getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(LocalDate originalDate) {
        this.originalDate = originalDate;
    }

    public String getOriginalShiftType() {
        return originalShiftType;
    }

    public void setOriginalShiftType(String originalShiftType) {
        this.originalShiftType = originalShiftType;
    }

    public String getOriginalLocation() {
        return originalLocation;
    }

    public void setOriginalLocation(String originalLocation) {
        this.originalLocation = originalLocation;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getRequestedShiftType() {
        return requestedShiftType;
    }

    public void setRequestedShiftType(String requestedShiftType) {
        this.requestedShiftType = requestedShiftType;
    }

    public String getRequestedLocation() {
        return requestedLocation;
    }

    public void setRequestedLocation(String requestedLocation) {
        this.requestedLocation = requestedLocation;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getAdminDecision() {
        return adminDecision;
    }

    public void setAdminDecision(String adminDecision) {
        this.adminDecision = adminDecision;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}