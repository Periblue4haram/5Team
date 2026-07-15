package com.example.myeongranghoe.controller;

import com.example.myeongranghoe.service.EmailVerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final EmailVerificationService emailVerificationService;

    public AuthController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@Valid @RequestBody EmailRequest request) {
        var result = emailVerificationService.sendVerificationCode(request.email());
        // LinkedHashMap allows null "code" when SMTP delivered the mail (do not expose OTP).
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("success", true);
        body.put("message", result.message());
        body.put("delivered", result.delivered());
        body.put("expiresInSeconds", result.expiresInSeconds());
        if (result.code() != null) {
            body.put("code", result.code());
        }
        return ResponseEntity.ok(body);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        boolean verified = emailVerificationService.verifyCode(request.email(), request.code());
        if (verified) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "verified", true,
                    "message", "인증번호가 확인되었어요."
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "verified", false,
                "message", "인증번호가 올바르지 않거나 만료됐어요."
        ));
    }

    public record EmailRequest(
            @NotBlank(message = "이메일은 필수입니다")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+ac\\.kr$", message = "학교 이메일 형식만 허용합니다")
            String email
    ) {}

    public record VerifyCodeRequest(
            @NotBlank(message = "이메일은 필수입니다")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+ac\\.kr$", message = "학교 이메일 형식만 허용합니다")
            String email,
            @NotBlank(message = "인증번호는 필수입니다")
            String code
    ) {}
}
