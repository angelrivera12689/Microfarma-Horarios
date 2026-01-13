package MicrofarmaHorarios.HumanResources.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.HumanResources.Entity.Position;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesPositionService;

@RestController
@RequestMapping("/api/humanresources/positions")
public class HumanResourcesPositionController extends AHumanResourcesBaseController<Position, IHumanResourcesPositionService> {

    public HumanResourcesPositionController(IHumanResourcesPositionService service) {
        super(service, "Position");
    }

}