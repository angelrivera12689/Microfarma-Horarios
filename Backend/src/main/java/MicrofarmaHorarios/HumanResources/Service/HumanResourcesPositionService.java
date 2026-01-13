package MicrofarmaHorarios.HumanResources.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.Position;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesBaseRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesPositionRepository;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesPositionService;

@Service
public class HumanResourcesPositionService extends AHumanResourcesBaseService<Position> implements IHumanResourcesPositionService {

    @Autowired
    private IHumanResourcesPositionRepository positionRepository;

    @Override
    protected IHumanResourcesBaseRepository<Position, String> getRepository() {
        return positionRepository;
    }

}