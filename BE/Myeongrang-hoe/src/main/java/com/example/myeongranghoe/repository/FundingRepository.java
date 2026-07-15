package com.example.myeongranghoe.repository;

import com.example.myeongranghoe.domain.Funding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundingRepository extends JpaRepository<Funding, Long> {
    List<Funding> findAllByOrderByCreatedAtDesc();

    List<Funding> findByHostEmailOrderByCreatedAtDesc(String hostEmail);

    List<Funding> findByParticipantsContaining(String email);
}
