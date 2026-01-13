package MicrofarmaHorarios.Schedules.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import MicrofarmaHorarios.Schedules.Entity.ASchedulesBaseEntity;

public interface ISchedulesBaseRepository<T extends ASchedulesBaseEntity, ID> extends JpaRepository<T, ID>{
    // Métodos comunes para repositorios
}