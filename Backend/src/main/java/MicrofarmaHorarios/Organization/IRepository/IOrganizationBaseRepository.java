package MicrofarmaHorarios.Organization.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import MicrofarmaHorarios.Organization.Entity.AOrganizationBaseEntity;

public interface IOrganizationBaseRepository<T extends AOrganizationBaseEntity, ID> extends JpaRepository<T, ID>{
    // Métodos comunes para repositorios
}