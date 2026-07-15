package com.example.myeongranghoe.service;

import com.example.myeongranghoe.config.MailProperties;
import com.example.myeongranghoe.domain.EmailVerification;
import com.example.myeongranghoe.repository.EmailVerificationRepository;
import com.example.myeongranghoe.repository.UserAccountRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int CODE_TTL_MINUTES = 5;
    private static final int VERIFIED_TTL_MINUTES = 30;

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailProperties mailProperties;
    private final String mailUsername;
    private final String mailPassword;
    private final Random random = new Random();

    public EmailVerificationService(
            JavaMailSender javaMailSender,
            EmailVerificationRepository emailVerificationRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            MailProperties mailProperties,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.javaMailSender = javaMailSender;
        this.emailVerificationRepository = emailVerificationRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailProperties = mailProperties;
        this.mailUsername = mailUsername == null ? "" : mailUsername.trim();
        this.mailPassword = mailPassword == null ? "" : mailPassword.trim();
    }

    public boolean isSmtpConfigured() {
        return hasSmtpCredentials();
    }

    public String resolveFromAddress() {
        if (mailProperties.getFrom() != null && !mailProperties.getFrom().isBlank()) {
            return mailProperties.getFrom().trim();
        }
        return mailUsername;
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
        boolean exposeCode = !delivered && mailProperties.isExposeCodeWhenUndelivered();
        String message = delivered
                ? "인증번호를 메일로 전송했어요. 메일함을 확인해주세요."
                : (hasSmtpCredentials()
                    ? "인증번호를 생성했지만 메일 전송에 실패했어요. SMTP 설정(MAIL_USERNAME/MAIL_PASSWORD)을 다시 확인해주세요."
                    : "인증번호를 생성했어요. SMTP 미설정 개발 모드입니다. MAIL_USERNAME·MAIL_PASSWORD를 설정하면 실제 메일이 발송됩니다.");
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
            log.info("SMTP not configured — dev mode OTP for {}", email);
            return false;
        }

        String from = resolveFromAddress();
        if (from == null || from.isBlank()) {
            log.warn("SMTP username/from is empty — cannot send mail");
            return false;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("[명랑회] 학교 이메일 인증번호");
            helper.setText(
                    "<div style=\"font-family: Arial, sans-serif; line-height:1.5;\">"
                            + "<p>안녕하세요. <strong>명랑회</strong> 이메일 인증번호입니다.</p>"
                            + "<h2 style=\"color:#116AD4; letter-spacing:4px;\">" + code + "</h2>"
                            + "<p>인증번호는 <strong>" + CODE_TTL_MINUTES + "분</strong> 안에 입력해주세요.</p>"
                            + "<p style=\"color:#888; font-size:12px;\">본인이 요청하지 않았다면 이 메일을 무시하세요.</p>"
                            + "</div>",
                    true
            );
            javaMailSender.send(message);
            log.info("Verification mail delivered to {}", email);
            return true;
        } catch (Exception ex) {
            log.warn("Email send failed for {}: {}", email, ex.getMessage());
            return false;
        }
    }

    public record VerificationResult(String code, boolean delivered, String message, int expiresInSeconds) {}
}
