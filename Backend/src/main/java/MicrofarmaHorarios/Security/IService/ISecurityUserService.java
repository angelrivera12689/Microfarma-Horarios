package MicrofarmaHorarios.Security.IService;

import java.util.Optional;

import MicrofarmaHorarios.Security.Entity.User;

public interface ISecurityUserService extends ISecurityBaseService<User> {

    Optional<User> findByEmail(String email) throws Exception;

    void updatePassword(User user, String encodedNewPassword) throws Exception;

}
