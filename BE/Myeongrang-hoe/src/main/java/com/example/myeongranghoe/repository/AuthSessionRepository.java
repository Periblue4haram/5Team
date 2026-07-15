package com.example.myeongranghoe.repository;

import com.example.myeongranghoe.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {
    Optional<AuthSession> findByToken(String token);

    void deleteByEmail(String email);
}
