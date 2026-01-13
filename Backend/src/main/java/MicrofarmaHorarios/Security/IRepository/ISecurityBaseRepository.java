package MicrofarmaHorarios.Security.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import MicrofarmaHorarios.Security.Entity.ASecurityBaseEntity;

public interface ISecurityBaseRepository<T extends ASecurityBaseEntity, ID> extends JpaRepository<T, ID>{
    // Métodos comunes para repositorios
}
