package MicrofarmaHorarios.Organization.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Organization.Entity.Company;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationBaseRepository;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationCompanyRepository;
import MicrofarmaHorarios.Organization.IService.IOrganizationCompanyService;

@Service
public class OrganizationCompanyService extends AOrganizationBaseService<Company> implements IOrganizationCompanyService {

    @Autowired
    private IOrganizationCompanyRepository companyRepository;

    @Override
    protected IOrganizationBaseRepository<Company, String> getRepository() {
        return companyRepository;
    }

    @Override
    public Optional<Company> findByName(String name) throws Exception {
        return companyRepository.findByName(name);
    }

}