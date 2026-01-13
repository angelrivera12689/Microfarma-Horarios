package MicrofarmaHorarios.Schedules.IRepository;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Schedules.Entity.ShiftType;

@Repository
public interface ISchedulesShiftTypeRepository extends ISchedulesBaseRepository<ShiftType, String> {

}