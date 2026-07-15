package com.example.myeongranghoe.repository;

import com.example.myeongranghoe.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByFundingIdOrderByCreatedAtAsc(Long fundingId);
}
