package com.example.myeongranghoe.repository;

import com.example.myeongranghoe.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByFundingIdOrderByCreatedAtAsc(Long fundingId);
}
