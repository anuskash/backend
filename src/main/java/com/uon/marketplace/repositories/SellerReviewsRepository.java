package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.SellerReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerReviewsRepository extends JpaRepository<SellerReviews, Long> {
    List<SellerReviews> findBySellerId(Long sellerId);

    List<SellerReviews> findByReviewerId(Long reviewerId);
}
