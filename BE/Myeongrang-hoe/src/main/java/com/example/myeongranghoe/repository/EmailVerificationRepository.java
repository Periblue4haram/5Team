package com.example.myeongranghoe.repository;

import com.example.myeongranghoe.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
}
