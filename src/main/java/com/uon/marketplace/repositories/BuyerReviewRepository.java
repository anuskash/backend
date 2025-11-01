package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.BuyerReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerReviewRepository extends JpaRepository<BuyerReviews, Long> {
    List<BuyerReviews> findByBuyerId(Long buyerId);
    List<BuyerReviews> findByReviewerId(Long reviewerId);
}
