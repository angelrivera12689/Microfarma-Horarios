package MicrofarmaHorarios.Schedules.IRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Schedules.Entity.RequestStatus;
import MicrofarmaHorarios.Schedules.Entity.ShiftChangeRequest;

@Repository
public interface ISchedulesShiftChangeRequestRepository extends ISchedulesBaseRepository<ShiftChangeRequest, String> {

    List<ShiftChangeRequest> findByEmployee_Id(String employeeId);

    List<ShiftChangeRequest> findByRequestStatus(RequestStatus status);

    List<ShiftChangeRequest> findByEmployee_IdAndRequestStatus(String employeeId, RequestStatus status);

}