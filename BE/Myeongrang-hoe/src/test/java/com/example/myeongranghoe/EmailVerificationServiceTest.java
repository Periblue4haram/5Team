package com.example.myeongranghoe;

import com.example.myeongranghoe.service.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmailVerificationServiceTest {
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    void sendVerificationCodeReturnsCodeAndCanBeVerifiedOnce() {
        var result = emailVerificationService.sendVerificationCode("student@mju.ac.kr");

        assertThat(result.code()).isNotBlank();
        assertThat(result.code()).hasSize(6);
        assertThat(result.delivered()).isFalse();
        assertThat(emailVerificationService.verifyCode("student@mju.ac.kr", result.code())).isTrue();
        // OTP is one-time use
        assertThat(emailVerificationService.verifyCode("student@mju.ac.kr", result.code())).isFalse();
    }

    @Test
    void wrongCodeIsRejected() {
        var result = emailVerificationService.sendVerificationCode("other@mju.ac.kr");
        assertThat(emailVerificationService.verifyCode("other@mju.ac.kr", "000000")).isFalse();
        assertThat(emailVerificationService.verifyCode("other@mju.ac.kr", result.code())).isTrue();
    }
}
