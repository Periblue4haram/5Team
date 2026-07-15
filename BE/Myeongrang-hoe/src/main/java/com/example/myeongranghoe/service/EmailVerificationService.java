package com.example.myeongranghoe.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {
    private final JavaMailSender javaMailSender;
    private final String mailUsername;
    private final String mailPassword;
    private final Map<String, VerificationEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public EmailVerificationService(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.javaMailSender = javaMailSender;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    public VerificationResult sendVerificationCode(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String code = String.format("%06d", 100000 + random.nextInt(900000));
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);
        store.put(normalizedEmail, new VerificationEntry(code, expiresAt));

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
                300
        );
    }

    public boolean verifyCode(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        VerificationEntry entry = store.get(normalizedEmail);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(normalizedEmail);
            return false;
        }
        if (!entry.code().equals(code.trim())) {
            return false;
        }
        // One-time use: drop after successful verification.
        store.remove(normalizedEmail);
        return true;
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

    private record VerificationEntry(String code, Instant expiresAt) {}
}
