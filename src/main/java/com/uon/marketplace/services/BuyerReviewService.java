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
    public BuyerReviews updateReview(Long reviewId, BuyerReviewRequest request) {
        return buyerReviewRepository.findById(reviewId)
                .map(review -> {
                    review.setRating(request.getRating());
                    review.setReviewText(request.getReviewText());
                    return buyerReviewRepository.save(review);
                })
                .orElseThrow(() -> new RuntimeException("Review not found"));
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
    public List<BuyerReviews> getReviewsByProductId(Long productId) {
        return buyerReviewRepository.findAll().stream()
                .filter(review -> review.getProductId().equals(productId))
                .toList();
    }
    public Double getAverageRatingForBuyer(Long userId) {
        List<BuyerReviews> reviews = buyerReviewRepository.findByBuyerId(userId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream().mapToDouble(BuyerReviews::getRating).sum();
        return sum / reviews.size();
    }
}
