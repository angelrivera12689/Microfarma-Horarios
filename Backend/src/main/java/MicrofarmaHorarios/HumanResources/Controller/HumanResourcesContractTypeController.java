package MicrofarmaHorarios.HumanResources.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.HumanResources.Entity.ContractType;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesContractTypeService;

@RestController
@RequestMapping("/api/humanresources/contracttypes")
public class HumanResourcesContractTypeController extends AHumanResourcesBaseController<ContractType, IHumanResourcesContractTypeService> {

    public HumanResourcesContractTypeController(IHumanResourcesContractTypeService service) {
        super(service, "ContractType");
    }

}