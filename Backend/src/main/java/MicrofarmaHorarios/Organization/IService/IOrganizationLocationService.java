package MicrofarmaHorarios.Organization.IService;

import java.util.List;

import MicrofarmaHorarios.Organization.Entity.Location;

public interface IOrganizationLocationService extends IOrganizationBaseService<Location> {

    List<Location> findByCompanyId(String companyId) throws Exception;

}