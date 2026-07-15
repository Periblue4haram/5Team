package com.example.myeongranghoe.controller;

import com.example.myeongranghoe.service.EmailVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mail")
public class MailStatusController {
    private final EmailVerificationService emailVerificationService;

    public MailStatusController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * SMTP 설정 여부만 노출합니다. 비밀번호·토큰은 절대 반환하지 않습니다.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        boolean configured = emailVerificationService.isSmtpConfigured();
        String from = emailVerificationService.resolveFromAddress();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("smtpConfigured", configured);
        body.put("fromConfigured", from != null && !from.isBlank());
        body.put("mode", configured ? "smtp" : "dev");
        body.put(
                "message",
                configured
                        ? "SMTP 자격 증명이 설정되어 있습니다. 인증 메일이 실제 발송됩니다."
                        : "SMTP 미설정 개발 모드입니다. MAIL_USERNAME / MAIL_PASSWORD 를 설정하세요."
        );
        return ResponseEntity.ok(body);
    }
}
