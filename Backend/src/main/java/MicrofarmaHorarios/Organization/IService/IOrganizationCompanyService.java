package MicrofarmaHorarios.Organization.IService;

import java.util.Optional;

import MicrofarmaHorarios.Organization.Entity.Company;

public interface IOrganizationCompanyService extends IOrganizationBaseService<Company> {

    Optional<Company> findByName(String name) throws Exception;

}