package MicrofarmaHorarios.Schedules.IRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Schedules.Entity.RequestStatus;
import MicrofarmaHorarios.Schedules.Entity.ShiftChangeRequest;

@Repository
public interface IShiftChangeRequestRepository extends JpaRepository<ShiftChangeRequest, String>, ISchedulesBaseRepository<ShiftChangeRequest, String> {

    // Find requests by employee ID
    List<ShiftChangeRequest> findByEmployeeId(String employeeId);

    // Find pending requests
    List<ShiftChangeRequest> findByStatus(RequestStatus status);

    // Find requests by employee and status
    List<ShiftChangeRequest> findByEmployeeIdAndStatus(String employeeId, RequestStatus status);

    // Check if employee has pending request for a specific shift
    @Query("SELECT COUNT(r) > 0 FROM ShiftChangeRequest r WHERE r.employee.id = :employeeId AND r.originalShift.id = :shiftId AND r.status = :status")
    boolean existsPendingRequestForShift(@Param("employeeId") String employeeId, @Param("shiftId") String shiftId, @Param("status") RequestStatus status);
}