package MicrofarmaHorarios.HumanResources.IRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.HumanResources.Entity.Employee;

@Repository
public interface IHumanResourcesEmployeeRepository extends IHumanResourcesBaseRepository<Employee, String> {

    Optional<Employee> findByEmail(String email);

}