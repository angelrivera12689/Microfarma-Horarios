package MicrofarmaHorarios.Schedules.IRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.Schedules.Entity.Shift;

@Repository
public interface ISchedulesShiftRepository extends ISchedulesBaseRepository<Shift, String> {

    List<Shift> findByEmployeeId(String employeeId);

    List<Shift> findByLocationId(String locationId);

    List<Shift> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Shift> findByDateBetweenAndLocationId(LocalDate startDate, LocalDate endDate, String locationId);
    
    Optional<Shift> findByEmployeeAndDate(Employee employee, LocalDate date);

    Optional<Shift> findByEmployeeAndDateAndStatusTrue(Employee employee, LocalDate date);

    // New method to find any shift (active or deleted) for employee and date
    Optional<Shift> findByEmployeeAndDateAndDeletedAtIsNull(Employee employee, LocalDate date);

    // Custom update methods for safe partial updates
    @Modifying
    @Query("UPDATE Shift s SET s.shiftType.id = :shiftTypeId, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :shiftId")
    void updateShiftTypeOnly(@Param("shiftId") String shiftId, @Param("shiftTypeId") String shiftTypeId);

    @Modifying
    @Query("UPDATE Shift s SET s.notes = :notes, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :shiftId")
    void updateShiftNotes(@Param("shiftId") String shiftId, @Param("notes") String notes);

}