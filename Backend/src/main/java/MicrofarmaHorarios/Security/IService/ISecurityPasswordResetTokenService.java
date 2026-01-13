package MicrofarmaHorarios.Security.IService;

import java.util.Optional;

import MicrofarmaHorarios.Security.Entity.PasswordResetToken;
import MicrofarmaHorarios.Security.Entity.User;

public interface ISecurityPasswordResetTokenService extends ISecurityBaseService<PasswordResetToken> {

    PasswordResetToken createToken(User user) throws Exception;

    Optional<PasswordResetToken> findByToken(String token) throws Exception;

    void invalidateToken(String token) throws Exception;

    boolean isTokenValid(String token) throws Exception;
}