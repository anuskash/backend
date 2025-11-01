package com.uon.marketplace.services;

import com.uon.marketplace.dto.requests.BuyerReviewRequest;
import com.uon.marketplace.entities.BuyerReviews;
import com.uon.marketplace.repositories.BuyerReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BuyerReviewService {
    @Autowired
    private BuyerReviewRepository buyerReviewRepository;

    public BuyerReviews createReview(BuyerReviewRequest request) {
        BuyerReviews review = new BuyerReviews();
        review.setReviewerId(request.getReviewerId());
        review.setBuyerId(request.getBuyerId());
        review.setRating(request.getRating());
        review.setProductId(request.getProductId());
        review.setReviewText(request.getReviewText());
        return buyerReviewRepository.save(review);
    }

    public Optional<BuyerReviews> getReviewById(Long reviewId) {
        return buyerReviewRepository.findById(reviewId);
    }

    public List<BuyerReviews> getReviewsByBuyerId(Long buyerId) {
        return buyerReviewRepository.findByBuyerId(buyerId);
    }

    public List<BuyerReviews> getReviewsByReviewerId(Long reviewerId) {
        return buyerReviewRepository.findByReviewerId(reviewerId);
    }

    public void deleteReview(Long reviewId) {
        buyerReviewRepository.deleteById(reviewId);
    }
}
