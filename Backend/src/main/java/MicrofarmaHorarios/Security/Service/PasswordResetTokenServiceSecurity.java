package MicrofarmaHorarios.Security.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MicrofarmaHorarios.Security.IRepository.ISecurityBaseRepository;
import MicrofarmaHorarios.Security.IRepository.ISecurityPasswordResetTokenRepository;
import MicrofarmaHorarios.Security.Entity.PasswordResetToken;
import MicrofarmaHorarios.Security.Entity.User;
import MicrofarmaHorarios.Security.IService.ISecurityPasswordResetTokenService;

@Service
public class PasswordResetTokenServiceSecurity extends ASecurityBaseService<PasswordResetToken> implements ISecurityPasswordResetTokenService {

    @Override
    protected ISecurityBaseRepository<PasswordResetToken, String> getRepository() {
        return repository;
    }

    @Autowired
    private ISecurityPasswordResetTokenRepository repository;

    @Override
    public PasswordResetToken createToken(User user) throws Exception {
        // Invalidate any existing unused token for the user
        Optional<PasswordResetToken> existingToken = repository.findByUser_IdAndUsedFalse(user.getId());
        if (existingToken.isPresent()) {
            existingToken.get().setUsed(true);
            repository.save(existingToken.get());
        }

        // Create new token
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(generateToken());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // 15 minutes expiry
        token.setUsed(false);
        token.setStatus(true);
        token.setCreatedAt(LocalDateTime.now());
        token.setCreatedBy("system");

        return repository.save(token);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) throws Exception {
        return repository.findByToken(token);
    }

    @Override
    @Transactional
    public void invalidateToken(String token) throws Exception {
        Optional<PasswordResetToken> resetToken = repository.findByToken(token);
        if (resetToken.isPresent()) {
            resetToken.get().setUsed(true);
            repository.save(resetToken.get());
        }
    }

    @Override
    public boolean isTokenValid(String token) throws Exception {
        System.out.println("Checking token validity for: " + token);
        if (repository == null) {
            System.err.println("Repository is null");
            throw new Exception("Repository not available");
        }
        Optional<PasswordResetToken> resetToken = repository.findByToken(token);
        System.out.println("Token found in DB: " + resetToken.isPresent());
        if (resetToken.isPresent()) {
            PasswordResetToken t = resetToken.get();
            boolean notUsed = !t.getUsed();
            boolean notExpired = t.getExpiryDate().isAfter(LocalDateTime.now());
            System.out.println("Token not used: " + notUsed + ", not expired: " + notExpired + ", expiry: " + t.getExpiryDate() + ", now: " + LocalDateTime.now());
            return notUsed && notExpired;
        }
        return false;
    }

    private String generateToken() {
        // Generate a 6-digit numeric code
        int code = (int) (Math.random() * 900000) + 100000; // 100000 to 999999
        return String.valueOf(code);
    }
}