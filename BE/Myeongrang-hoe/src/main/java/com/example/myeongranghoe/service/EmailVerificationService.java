package com.example.myeongranghoe.service;

import com.example.myeongranghoe.domain.EmailVerification;
import com.example.myeongranghoe.repository.EmailVerificationRepository;
import com.example.myeongranghoe.repository.UserAccountRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Random;

@Service
public class EmailVerificationService {
    private static final int CODE_TTL_MINUTES = 5;
    private static final int VERIFIED_TTL_MINUTES = 30;

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String mailUsername;
    private final String mailPassword;
    private final Random random = new Random();

    public EmailVerificationService(
            JavaMailSender javaMailSender,
            EmailVerificationRepository emailVerificationRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.javaMailSender = javaMailSender;
        this.emailVerificationRepository = emailVerificationRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    @Transactional
    public VerificationResult sendVerificationCode(String email) {
        String normalizedEmail = normalize(email);
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일이에요. 로그인 탭을 이용해주세요.");
        }

        String code = String.format("%06d", 100000 + random.nextInt(900000));
        Instant expiresAt = Instant.now().plus(CODE_TTL_MINUTES, ChronoUnit.MINUTES);

        EmailVerification verification = emailVerificationRepository.findById(normalizedEmail)
                .orElseGet(EmailVerification::new);
        verification.setEmail(normalizedEmail);
        verification.setCodeHash(passwordEncoder.encode(code));
        verification.setCodeExpiresAt(expiresAt);
        verification.setVerified(false);
        verification.setVerifiedExpiresAt(null);
        emailVerificationRepository.save(verification);

        boolean delivered = sendMail(normalizedEmail, code);
        boolean exposeCode = !delivered;
        String message = delivered
                ? "인증번호를 메일로 전송했어요."
                : (hasSmtpCredentials()
                    ? "인증번호를 생성했지만 메일 전송에 실패했어요. SMTP 설정을 다시 확인해주세요."
                    : "인증번호를 생성했어요. 현재는 SMTP 인증 정보가 없어 개발 모드로 응답하고 있습니다. MAIL_USERNAME와 MAIL_PASSWORD를 설정하면 실제 메일이 발송됩니다.");
        return new VerificationResult(
                exposeCode ? code : null,
                delivered,
                message,
                CODE_TTL_MINUTES * 60
        );
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        String normalizedEmail = normalize(email);
        EmailVerification verification = emailVerificationRepository.findById(normalizedEmail).orElse(null);
        if (verification == null || verification.getCodeHash() == null || verification.getCodeExpiresAt() == null) {
            return false;
        }
        if (Instant.now().isAfter(verification.getCodeExpiresAt())) {
            emailVerificationRepository.delete(verification);
            return false;
        }
        if (!passwordEncoder.matches(code.trim(), verification.getCodeHash())) {
            return false;
        }

        verification.setVerified(true);
        verification.setVerifiedExpiresAt(Instant.now().plus(VERIFIED_TTL_MINUTES, ChronoUnit.MINUTES));
        verification.setCodeHash(null);
        verification.setCodeExpiresAt(null);
        emailVerificationRepository.save(verification);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isEmailVerifiedForSignup(String email) {
        String normalizedEmail = normalize(email);
        EmailVerification verification = emailVerificationRepository.findById(normalizedEmail).orElse(null);
        if (verification == null || !verification.isVerified() || verification.getVerifiedExpiresAt() == null) {
            return false;
        }
        return Instant.now().isBefore(verification.getVerifiedExpiresAt());
    }

    @Transactional
    public void clearVerification(String email) {
        emailVerificationRepository.deleteById(normalize(email));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasSmtpCredentials() {
        return mailUsername != null && !mailUsername.isBlank() && mailPassword != null && !mailPassword.isBlank();
    }

    private boolean sendMail(String email, String code) {
        if (!hasSmtpCredentials()) {
            return false;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[명랑회] 학교 이메일 인증번호");
            helper.setText(
                    "<div style=\"font-family: Arial, sans-serif;\">"
                            + "<p>안녕하세요. 명랑회 인증번호는 아래와 같습니다.</p>"
                            + "<h2 style=\"color:#116AD4\">" + code + "</h2>"
                            + "<p>5분 안에 입력해주세요.</p>"
                            + "</div>",
                    true
            );
            javaMailSender.send(message);
            return true;
        } catch (Exception ex) {
            System.out.printf("Email send failed for %s: %s%n", email, ex.getMessage());
            return false;
        }
    }

    public record VerificationResult(String code, boolean delivered, String message, int expiresInSeconds) {}
}
