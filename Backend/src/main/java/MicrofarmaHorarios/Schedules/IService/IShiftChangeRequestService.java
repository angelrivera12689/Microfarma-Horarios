package MicrofarmaHorarios.Schedules.IService;

import java.util.List;

import MicrofarmaHorarios.Schedules.DTO.Request.ShiftChangeRequestDto;
import MicrofarmaHorarios.Schedules.DTO.Request.ShiftChangeDecisionDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ShiftChangeRequestResponseDto;
import MicrofarmaHorarios.Schedules.Entity.RequestStatus;

public interface IShiftChangeRequestService {

    // Create a new shift change request
    ShiftChangeRequestResponseDto createRequest(ShiftChangeRequestDto requestDto, String employeeId);

    // Get requests for a specific employee
    List<ShiftChangeRequestResponseDto> getRequestsByEmployee(String employeeId);

    // Get pending requests for admin review
    List<ShiftChangeRequestResponseDto> getPendingRequests();

    // Admin decides on a request (approve/reject)
    ShiftChangeRequestResponseDto decideOnRequest(String requestId, ShiftChangeDecisionDto decisionDto, String adminId);

    // Get requests by status
    List<ShiftChangeRequestResponseDto> getRequestsByStatus(RequestStatus status);

    // Cancel a request (by employee)
    void cancelRequest(String requestId, String employeeId);
}