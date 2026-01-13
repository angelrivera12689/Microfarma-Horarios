package MicrofarmaHorarios.Schedules.IService;

import java.util.List;

import MicrofarmaHorarios.Schedules.Entity.RequestStatus;
import MicrofarmaHorarios.Schedules.Entity.ShiftChangeRequest;

public interface ISchedulesShiftChangeRequestService extends ISchedulesBaseService<ShiftChangeRequest> {

    List<ShiftChangeRequest> findByEmployeeId(String employeeId) throws Exception;

    List<ShiftChangeRequest> findByRequestStatus(RequestStatus status) throws Exception;

    List<ShiftChangeRequest> findByEmployeeIdAndRequestStatus(String employeeId, RequestStatus status) throws Exception;

    ShiftChangeRequest createRequest(ShiftChangeRequest request) throws Exception;

    ShiftChangeRequest approveRequest(String requestId, String adminDecision, String adminId) throws Exception;

    ShiftChangeRequest denyRequest(String requestId, String adminDecision, String adminId) throws Exception;

}