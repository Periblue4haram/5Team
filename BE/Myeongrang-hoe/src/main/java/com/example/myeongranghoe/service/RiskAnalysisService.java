package com.example.myeongranghoe.service;

import com.example.myeongranghoe.domain.Funding;
import com.example.myeongranghoe.domain.UserAccount;
import com.example.myeongranghoe.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

/**
 * PRD 노쇼 리스크의 규칙 기반 1차 구현 (Claude 연동 전 로컬 분석).
 */
@Service
public class RiskAnalysisService {
    private final UserAccountRepository userAccountRepository;

    public RiskAnalysisService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public String analyze(Funding funding) {
        int score = 0;
        if (funding.getDescription() != null && funding.getDescription().trim().length() >= 30) {
            score += 2;
        }
        if (funding.getAddress() != null && !funding.getAddress().isBlank()) {
            score += 1;
        }
        if (funding.getMeetAt() != null && !funding.getMeetAt().isBlank()) {
            score += 1;
        }
        if (funding.getDeadlineAt() != null && !funding.getDeadlineAt().isBlank()) {
            score += 1;
        }

        UserAccount host = userAccountRepository.findByEmail(funding.getHostEmail()).orElse(null);
        if (host != null) {
            if (host.getNoShowCount() >= 2) {
                score -= 3;
            } else if (host.getNoShowCount() == 1) {
                score -= 1;
            }
            if (host.getSunlightScore() >= 70) {
                score += 2;
            } else if (host.getSunlightScore() < 30) {
                score -= 1;
            }
        }

        if (score >= 4) {
            return "낮음";
        }
        if (score >= 1) {
            return "중간";
        }
        return "높음";
    }

    public String buildNudgeMessage(Funding funding) {
        int remain = Math.max(0, funding.getTargetCount() - funding.currentCount());
        String category = funding.getCategory() == null ? "모임" : funding.getCategory();
        if (remain == 0) {
            return "목표 인원이 모두 모였어요! 채팅방에서 약속을 확정해보세요.";
        }
        if (remain == 1) {
            return "🍽️ 딱 한 명만 더 모이면 \"" + funding.getTitle() + "\" " + category + " 모임을 시작할 수 있어요!";
        }
        if (remain <= 3) {
            return "성사까지 " + remain + "명! 관심 있는 친구를 초대해볼까요?";
        }
        return "\"" + funding.getTitle() + "\" 모집이 진행 중이에요. 주변 친구들과 함께 참여해보세요.";
    }
}
