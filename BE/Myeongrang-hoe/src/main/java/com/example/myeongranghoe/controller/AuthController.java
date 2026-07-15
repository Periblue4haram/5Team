package com.example.myeongranghoe.controller;

import com.example.myeongranghoe.dto.UserResponse;
import com.example.myeongranghoe.service.AuthSessionService;
import com.example.myeongranghoe.service.EmailVerificationService;
import com.example.myeongranghoe.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final EmailVerificationService emailVerificationService;
    private final UserService userService;
    private final AuthSessionService authSessionService;

    public AuthController(
            EmailVerificationService emailVerificationService,
            UserService userService,
            AuthSessionService authSessionService) {
        this.emailVerificationService = emailVerificationService;
        this.userService = userService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@Valid @RequestBody EmailRequest request) {
        var result = emailVerificationService.sendVerificationCode(request.email());
        var body = new LinkedHashMap<String, Object>();
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

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+ac\\.kr$", message = "학교 이메일 형식만 허용합니다")
            String email) {
        boolean taken = userService.isEmailTaken(email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "email", email.trim().toLowerCase(),
                "available", !taken,
                "taken", taken
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse user = userService.signUp(new UserService.SignUpCommand(
                request.email(),
                request.password(),
                request.name(),
                request.campus(),
                request.major(),
                request.age(),
                request.bio(),
                request.interests()
        ));
        String token = authSessionService.issueToken(user.email());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입이 완료되었어요.",
                "user", user,
                "accessToken", token
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        UserResponse user = userService.login(request.email(), request.password());
        String token = authSessionService.issueToken(user.email());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그인되었어요.",
                "user", user,
                "accessToken", token
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

    public record SignupRequest(
            @NotBlank(message = "이메일은 필수입니다")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+ac\\.kr$", message = "학교 이메일 형식만 허용합니다")
            String email,
            @NotBlank(message = "비밀번호는 필수입니다")
            @Size(min = 8, message = "비밀번호는 8자 이상이어야 해요")
            String password,
            @NotBlank(message = "이름은 필수입니다")
            @Size(max = 80, message = "이름은 80자 이하여야 해요")
            String name,
            @NotBlank(message = "캠퍼스는 필수입니다")
            String campus,
            String major,
            String age,
            String bio,
            List<String> interests
    ) {}

    public record LoginRequest(
            @NotBlank(message = "이메일은 필수입니다")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+ac\\.kr$", message = "학교 이메일 형식만 허용합니다")
            String email,
            @NotBlank(message = "비밀번호는 필수입니다")
            String password
    ) {}
}
