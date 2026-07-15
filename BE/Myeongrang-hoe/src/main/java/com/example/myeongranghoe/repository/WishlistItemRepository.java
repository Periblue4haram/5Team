package com.example.myeongranghoe.repository;

import com.example.myeongranghoe.domain.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    Optional<WishlistItem> findByUserEmailAndFundingId(String userEmail, Long fundingId);

    boolean existsByUserEmailAndFundingId(String userEmail, Long fundingId);
}
