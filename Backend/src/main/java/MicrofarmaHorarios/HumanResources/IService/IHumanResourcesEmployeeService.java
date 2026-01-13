package MicrofarmaHorarios.HumanResources.IService;

import java.util.Optional;

import MicrofarmaHorarios.HumanResources.Entity.Employee;

public interface IHumanResourcesEmployeeService extends IHumanResourcesBaseService<Employee> {

    Optional<Employee> findByEmail(String email) throws Exception;

}