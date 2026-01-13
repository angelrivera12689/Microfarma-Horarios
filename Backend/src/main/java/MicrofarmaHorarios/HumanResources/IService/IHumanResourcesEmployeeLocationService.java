package MicrofarmaHorarios.HumanResources.IService;

import java.util.List;

import MicrofarmaHorarios.HumanResources.Entity.EmployeeLocation;

public interface IHumanResourcesEmployeeLocationService extends IHumanResourcesBaseService<EmployeeLocation> {

    List<EmployeeLocation> findByEmployeeId(String employeeId) throws Exception;

    List<EmployeeLocation> findByLocationId(String locationId) throws Exception;

}