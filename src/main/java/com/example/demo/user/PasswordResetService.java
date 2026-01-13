package com.example.demo.user;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int EXPIRATION_MINUTES = 30;

    private final PasswordResetTokenRepository repository;

    public PasswordResetService(PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    public PasswordResetToken createToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        return repository.save(token);
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
}