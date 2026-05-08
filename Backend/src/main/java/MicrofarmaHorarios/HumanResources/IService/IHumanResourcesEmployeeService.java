package MicrofarmaHorarios.HumanResources.IService;

import java.util.List;
import java.util.Optional;

import MicrofarmaHorarios.HumanResources.Entity.Employee;

public interface IHumanResourcesEmployeeService extends IHumanResourcesBaseService<Employee> {

    Optional<Employee> findByEmail(String email) throws Exception;

    /**
     * Busca empleados por término de búsqueda en múltiples campos
     * @param searchTerm Término de búsqueda
     * @return Lista de empleados que coinciden
     */
    List<Employee> searchByTerm(String searchTerm) throws Exception;

}