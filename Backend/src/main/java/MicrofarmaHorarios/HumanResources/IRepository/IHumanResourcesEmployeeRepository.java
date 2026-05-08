package MicrofarmaHorarios.HumanResources.IRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.HumanResources.Entity.Employee;

@Repository
public interface IHumanResourcesEmployeeRepository extends IHumanResourcesBaseRepository<Employee, String> {

    Optional<Employee> findByEmail(String email);

    /**
     * Busca empleados por término de búsqueda en múltiples campos
     * @param searchTerm Término de búsqueda
     * @return Lista de empleados que coinciden
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "CAST(e.id AS string) LIKE CONCAT('%', ?1, '%')")
    List<Employee> searchByTerm(String searchTerm);
}