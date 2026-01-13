package MicrofarmaHorarios.Security.IRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Security.Entity.User;

@Repository
public interface ISecurityUserRepository extends ISecurityBaseRepository<User, String> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission WHERE u.email = :email")
    Optional<User> findByEmailWithRoleAndPermissions(@Param("email") String email);

}
