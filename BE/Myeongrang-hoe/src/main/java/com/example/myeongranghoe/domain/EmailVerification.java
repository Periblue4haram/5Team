package com.example.myeongranghoe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @Column(length = 190)
    private String email;

    @Column(name = "code_hash", length = 100)
    private String codeHash;

    @Column(name = "code_expires_at")
    private Instant codeExpiresAt;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "verified_expires_at")
    private Instant verifiedExpiresAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public Instant getCodeExpiresAt() {
        return codeExpiresAt;
    }

    public void setCodeExpiresAt(Instant codeExpiresAt) {
        this.codeExpiresAt = codeExpiresAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Instant getVerifiedExpiresAt() {
        return verifiedExpiresAt;
    }

    public void setVerifiedExpiresAt(Instant verifiedExpiresAt) {
        this.verifiedExpiresAt = verifiedExpiresAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
