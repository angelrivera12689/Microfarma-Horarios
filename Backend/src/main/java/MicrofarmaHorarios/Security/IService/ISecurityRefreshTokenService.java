package MicrofarmaHorarios.Security.IService;

import MicrofarmaHorarios.Security.Entity.RefreshToken;

public interface ISecurityRefreshTokenService {

    RefreshToken createRefreshToken(String userId, String userEmail);

    RefreshToken findByToken(String token);

    void deleteByUserId(String userId);

}