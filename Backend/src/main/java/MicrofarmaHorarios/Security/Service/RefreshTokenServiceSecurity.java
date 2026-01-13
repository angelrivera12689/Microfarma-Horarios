package MicrofarmaHorarios.Security.Service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Security.Entity.RefreshToken;

import MicrofarmaHorarios.Security.IRepository.ISecurityRefreshTokenRepository;

import MicrofarmaHorarios.Security.IService.ISecurityRefreshTokenService;

import MicrofarmaHorarios.Security.Utils.JwtUtils;

import java.util.Date;

@Service

public class RefreshTokenServiceSecurity implements ISecurityRefreshTokenService {

    @Autowired

    private ISecurityRefreshTokenRepository repository;

    @Autowired

    private JwtUtils jwtUtils;

    @Override

    public RefreshToken createRefreshToken(String userId, String userEmail) {

        String token = jwtUtils.generateRefreshToken(userEmail);

        Date expiration = new Date(System.currentTimeMillis() + jwtUtils.getRefreshExpirationMs());

        RefreshToken refreshToken = new RefreshToken(userId, token, expiration);

        return repository.save(refreshToken);

    }

    @Override

    public RefreshToken findByToken(String token) {

        return repository.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    }

    @Override

    public void deleteByUserId(String userId) {

        repository.deleteByUserId(userId);

    }

}

