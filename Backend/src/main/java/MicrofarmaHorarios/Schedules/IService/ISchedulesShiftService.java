package MicrofarmaHorarios.Schedules.IService;

import java.time.LocalDate;
import java.util.List;

import MicrofarmaHorarios.Schedules.Entity.Shift;

public interface ISchedulesShiftService extends ISchedulesBaseService<Shift> {

    List<Shift> findByEmployeeId(String employeeId) throws Exception;

    List<Shift> findByUserId(String userId) throws Exception;

    List<Shift> findByLocationId(String locationId) throws Exception;

    List<Shift> findByDateBetween(LocalDate startDate, LocalDate endDate) throws Exception;

    byte[] generateCalendarPdf(int year, int month, String locationId) throws Exception;

    byte[] generatePersonalShiftsPdf(String employeeId) throws Exception;

    List<Shift> saveAll(List<Shift> shifts) throws Exception;
}