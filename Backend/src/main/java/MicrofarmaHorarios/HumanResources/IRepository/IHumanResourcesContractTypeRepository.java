package MicrofarmaHorarios.HumanResources.IRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.HumanResources.Entity.ContractType;

@Repository
public interface IHumanResourcesContractTypeRepository extends IHumanResourcesBaseRepository<ContractType, String> {

    Optional<ContractType> findByNameIgnoreCase(String name);
    
    default Optional<ContractType> findFirstByNameIgnoreCase(String name) {
        return findByNameIgnoreCase(name);
    }
}