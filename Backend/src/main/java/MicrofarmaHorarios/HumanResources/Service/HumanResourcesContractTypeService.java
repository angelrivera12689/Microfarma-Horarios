package MicrofarmaHorarios.HumanResources.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.ContractType;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesBaseRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesContractTypeRepository;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesContractTypeService;

@Service
public class HumanResourcesContractTypeService extends AHumanResourcesBaseService<ContractType> implements IHumanResourcesContractTypeService {

    @Autowired
    private IHumanResourcesContractTypeRepository contractTypeRepository;

    @Override
    protected IHumanResourcesBaseRepository<ContractType, String> getRepository() {
        return contractTypeRepository;
    }

}