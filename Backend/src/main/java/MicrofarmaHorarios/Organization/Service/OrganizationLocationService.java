package MicrofarmaHorarios.Organization.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationBaseRepository;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationLocationRepository;
import MicrofarmaHorarios.Organization.IService.IOrganizationLocationService;

@Service
public class OrganizationLocationService extends AOrganizationBaseService<Location> implements IOrganizationLocationService {

    @Autowired
    private IOrganizationLocationRepository locationRepository;

    @Override
    protected IOrganizationBaseRepository<Location, String> getRepository() {
        return locationRepository;
    }

    @Override
    public List<Location> findByCompanyId(String companyId) throws Exception {
        return locationRepository.findByCompanyId(companyId);
    }

    @Override
    public java.util.Optional<Location> findByName(String name) throws Exception {
        return locationRepository.findByName(name);
    }

}