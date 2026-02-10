package MicrofarmaHorarios.HumanResources.IRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.HumanResources.Entity.Position;

@Repository
public interface IHumanResourcesPositionRepository extends IHumanResourcesBaseRepository<Position, String> {

    Optional<Position> findByNameIgnoreCase(String name);
    
    default Optional<Position> findFirstByNameIgnoreCase(String name) {
        return findByNameIgnoreCase(name);
    }
}