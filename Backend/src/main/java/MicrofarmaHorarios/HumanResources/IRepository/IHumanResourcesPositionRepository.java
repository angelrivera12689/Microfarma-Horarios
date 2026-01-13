package MicrofarmaHorarios.HumanResources.IRepository;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.HumanResources.Entity.Position;

@Repository
public interface IHumanResourcesPositionRepository extends IHumanResourcesBaseRepository<Position, String> {

}