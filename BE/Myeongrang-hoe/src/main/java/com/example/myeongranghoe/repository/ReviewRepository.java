package com.example.myeongranghoe.repository;

import com.example.myeongranghoe.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetEmailOrderByCreatedAtDesc(String targetEmail);

    List<Review> findByFundingIdOrderByCreatedAtDesc(Long fundingId);

    boolean existsByFundingIdAndWriterEmailAndTargetEmail(Long fundingId, String writerEmail, String targetEmail);
}
