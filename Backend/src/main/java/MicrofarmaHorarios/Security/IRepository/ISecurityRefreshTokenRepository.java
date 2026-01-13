package MicrofarmaHorarios.Security.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Security.Entity.RefreshToken;

import java.util.Optional;

@Repository

public interface ISecurityRefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(String userId);

}