package com.example.demo.user;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int EXPIRATION_MINUTES = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository repository;

    public PasswordResetService(PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    public PasswordResetToken createToken(User user) {
        invalidateActiveTokens(user);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        PasswordResetToken saved = repository.save(token);
        LOGGER.info("Password reset token generated for {}: /password-reset/{}", user.getEmail(), saved.getToken());
        return saved;
    }

    public Optional<PasswordResetToken> findValidToken(String tokenValue) {
        return repository.findByToken(tokenValue)
                .filter(token -> token.getUsedAt() == null)
                .filter(token -> token.getExpiresAt() != null && token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    public void markUsed(PasswordResetToken token) {
        token.setUsedAt(LocalDateTime.now());
        repository.save(token);
    }

    private void invalidateActiveTokens(User user) {
        List<PasswordResetToken> activeTokens = repository.findByUserAndUsedAtIsNull(user);
        if (activeTokens.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        activeTokens.forEach(token -> token.setUsedAt(now));
        repository.saveAll(activeTokens);
    }
}