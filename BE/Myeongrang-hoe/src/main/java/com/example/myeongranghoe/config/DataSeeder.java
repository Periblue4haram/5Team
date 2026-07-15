package com.example.myeongranghoe.config;

import com.example.myeongranghoe.domain.Funding;
import com.example.myeongranghoe.domain.UserAccount;
import com.example.myeongranghoe.repository.FundingRepository;
import com.example.myeongranghoe.repository.UserAccountRepository;
import com.example.myeongranghoe.service.RiskAnalysisService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {
    private final UserAccountRepository userAccountRepository;
    private final FundingRepository fundingRepository;
    private final PasswordEncoder passwordEncoder;
    private final RiskAnalysisService riskAnalysisService;

    public DataSeeder(
            UserAccountRepository userAccountRepository,
            FundingRepository fundingRepository,
            PasswordEncoder passwordEncoder,
            RiskAnalysisService riskAnalysisService) {
        this.userAccountRepository = userAccountRepository;
        this.fundingRepository = fundingRepository;
        this.passwordEncoder = passwordEncoder;
        this.riskAnalysisService = riskAnalysisService;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedIfMissing("test1@mju.ac.kr", "김명지", "인문캠퍼스", "컴퓨터공학과", "23");
        seedIfMissing("test2@mju.ac.kr", "이자연", "자연캠퍼스", "생명과학과", "21");
        seedFundingsIfEmpty();
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
        user.setInterests(List.of("맛집", "카페"));
        user.setSunlightScore(50);
        user.setLoginable(true);
        userAccountRepository.save(user);
    }

    private void seedFundingsIfEmpty() {
        if (fundingRepository.count() > 0) {
            return;
        }
        Funding a = baseFunding(
                "맛집",
                "홍대 맛집 탐방",
                "인문캠 근처에서 저녁 같이 먹어요. 매운 음식 환영!",
                "홍대입구역 9번 출구",
                "홍대 맛집 거리",
                37.5563,
                126.9236,
                "test1@mju.ac.kr",
                4,
                0
        );
        Funding b = baseFunding(
                "카페",
                "시험기간 카공 메이트",
                "도서관 대신 조용한 카페에서 같이 공부해요.",
                "명지대 인문캠퍼스 정문",
                "캠퍼스 카페",
                37.5805,
                126.9227,
                "test2@mju.ac.kr",
                3,
                0
        );
        a.setAiRisk(riskAnalysisService.analyze(a));
        b.setAiRisk(riskAnalysisService.analyze(b));
        fundingRepository.save(a);
        fundingRepository.save(b);
    }

    private Funding baseFunding(
            String category,
            String title,
            String description,
            String address,
            String locationName,
            double lat,
            double lng,
            String host,
            int target,
            int fee
    ) {
        Funding f = new Funding();
        f.setCategory(category);
        f.setTitle(title);
        f.setDescription(description);
        f.setAddress(address);
        f.setLocationName(locationName);
        f.setLat(lat);
        f.setLng(lng);
        f.setMeetAt("");
        f.setMeetTimeText("협의");
        f.setDeadlineAt("");
        f.setDeadlineText("모집 중");
        f.setTargetCount(target);
        f.setFee(fee);
        f.setHostEmail(host);
        f.setFillerParticipants(0);
        List<String> participants = new ArrayList<>();
        participants.add(host);
        f.setParticipants(participants);
        f.setMatched(false);
        f.setBest(false);
        return f;
    }
}
