package MicrofarmaHorarios.Schedules.IRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Schedules.Entity.ShiftType;

@Repository
public interface ISchedulesShiftTypeRepository extends ISchedulesBaseRepository<ShiftType, String> {
    Optional<ShiftType> findByNameIgnoreCase(String name);
    
    default Optional<ShiftType> findFirstByNameIgnoreCase(String name) {
        return findByNameIgnoreCase(name);
    }
}