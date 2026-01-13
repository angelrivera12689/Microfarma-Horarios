package MicrofarmaHorarios.Security.IRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Security.Entity.PasswordResetToken;

@Repository
public interface ISecurityPasswordResetTokenRepository extends ISecurityBaseRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser_IdAndUsedFalse(String userId);
}