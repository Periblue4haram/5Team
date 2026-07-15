package com.example.myeongranghoe.config;

import com.example.myeongranghoe.domain.UserAccount;
import com.example.myeongranghoe.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedIfMissing("test1@mju.ac.kr", "김명지", "인문캠퍼스", "컴퓨터공학과", "23");
        seedIfMissing("test2@mju.ac.kr", "이자연", "자연캠퍼스", "생명과학과", "21");
    }

    private void seedIfMissing(String email, String name, String campus, String major, String age) {
        if (userAccountRepository.existsByEmail(email)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("test1234"));
        user.setName(name);
        user.setCampus(campus);
        user.setMajor(major);
        user.setAge(age);
        user.setBio("");
        user.setInterests(List.of());
        user.setSunlightScore(50);
        user.setLoginable(true);
        userAccountRepository.save(user);
    }
}
