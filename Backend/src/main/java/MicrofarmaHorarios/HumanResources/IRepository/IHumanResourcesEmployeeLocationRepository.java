package MicrofarmaHorarios.HumanResources.IRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.HumanResources.Entity.EmployeeLocation;

@Repository
public interface IHumanResourcesEmployeeLocationRepository extends IHumanResourcesBaseRepository<EmployeeLocation, String> {

    List<EmployeeLocation> findByEmployeeId(String employeeId);

    List<EmployeeLocation> findByLocationId(String locationId);

}