package MicrofarmaHorarios.Organization.IRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Organization.Entity.Company;

@Repository
public interface IOrganizationCompanyRepository extends IOrganizationBaseRepository<Company, String> {

    Optional<Company> findByName(String name);

}