package MicrofarmaHorarios.News.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import MicrofarmaHorarios.News.Entity.ANewsBaseEntity;

public interface INewsBaseRepository<T extends ANewsBaseEntity, ID> extends JpaRepository<T, ID>{
    // Métodos comunes para repositorios
}