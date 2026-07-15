package com.example.myeongranghoe.service;

import com.example.myeongranghoe.domain.AuthSession;
import com.example.myeongranghoe.repository.AuthSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthSessionService {
    private static final int SESSION_DAYS = 14;

    private final AuthSessionRepository authSessionRepository;

    public AuthSessionService(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    @Transactional
    public String issueToken(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        AuthSession session = new AuthSession();
        session.setToken(token);
        session.setEmail(normalized);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(SESSION_DAYS, ChronoUnit.DAYS));
        authSessionRepository.save(session);
        return token;
    }

    @Transactional(readOnly = true)
    public Optional<String> resolveEmail(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return authSessionRepository.findByToken(token.trim())
                .filter(s -> s.getExpiresAt() != null && Instant.now().isBefore(s.getExpiresAt()))
                .map(AuthSession::getEmail);
    }
}
