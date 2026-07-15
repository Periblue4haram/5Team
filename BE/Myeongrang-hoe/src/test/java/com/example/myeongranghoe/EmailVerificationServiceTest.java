package com.example.myeongranghoe;

import com.example.myeongranghoe.service.EmailVerificationService;
import com.example.myeongranghoe.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class EmailVerificationServiceTest {
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserService userService;

    @Test
    void sendVerificationCodeReturnsCodeAndCanBeVerifiedOnce() {
        String email = uniqueEmail("verify");
        var result = emailVerificationService.sendVerificationCode(email);

        assertThat(result.code()).isNotBlank();
        assertThat(result.code()).hasSize(6);
        assertThat(result.delivered()).isFalse();
        assertThat(emailVerificationService.verifyCode(email, result.code())).isTrue();
        // OTP itself is one-time, but verification window remains for signup
        assertThat(emailVerificationService.verifyCode(email, result.code())).isFalse();
        assertThat(emailVerificationService.isEmailVerifiedForSignup(email)).isTrue();
    }

    @Test
    void wrongCodeIsRejected() {
        String email = uniqueEmail("wrong");
        var result = emailVerificationService.sendVerificationCode(email);
        assertThat(emailVerificationService.verifyCode(email, "000000")).isFalse();
        assertThat(emailVerificationService.verifyCode(email, result.code())).isTrue();
    }

    @Test
    void signupRequiresVerifiedEmailAndStoresHashedPassword() {
        String email = uniqueEmail("signup");
        var otp = emailVerificationService.sendVerificationCode(email);
        assertThat(emailVerificationService.verifyCode(email, otp.code())).isTrue();

        var user = userService.signUp(new UserService.SignUpCommand(
                email,
                "password123",
                "테스트",
                "인문캠퍼스",
                "컴퓨터공학과",
                "22",
                "소개",
                List.of("맛집", "카페")
        ));

        assertThat(user.email()).isEqualTo(email);
        assertThat(user.name()).isEqualTo("테스트");
        assertThat(user.interests()).containsExactly("맛집", "카페");
        assertThat(userService.isEmailTaken(email)).isTrue();
        assertThat(userService.login(email, "password123").email()).isEqualTo(email);
        assertThatThrownBy(() -> userService.login(email, "bad-password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String uniqueEmail(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8) + "@mju.ac.kr";
    }
}
