package MicrofarmaHorarios.Organization.IRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Organization.Entity.Location;

@Repository
public interface IOrganizationLocationRepository extends IOrganizationBaseRepository<Location, String> {

    List<Location> findByCompanyId(String companyId);

}