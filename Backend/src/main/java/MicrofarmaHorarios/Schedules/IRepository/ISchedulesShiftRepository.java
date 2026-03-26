package MicrofarmaHorarios.Schedules.IRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

}