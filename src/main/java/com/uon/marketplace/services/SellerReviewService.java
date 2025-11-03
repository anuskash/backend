package com.uon.marketplace.services;

import com.uon.marketplace.dto.requests.SellerReviewRequest;
import com.uon.marketplace.entities.SellerReviews;
import com.uon.marketplace.repositories.SellerReviewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerReviewService {
    @Autowired
    private SellerReviewsRepository sellerReviewsRepository;

    public SellerReviews createReview(SellerReviewRequest request) {
        SellerReviews review = new SellerReviews();
        review.setReviewerId(request.getReviewerId());
        review.setSellerId(request.getSellerId());
        review.setRating(request.getRating());
        review.setProductId(request.getProductId());
        review.setReviewText(request.getReviewText());
        return sellerReviewsRepository.save(review);
    }
    public SellerReviews updateReview(Long reviewId, SellerReviewRequest request) {
        return sellerReviewsRepository.findById(reviewId)
                .map(review -> {
                    review.setRating(request.getRating());
                    review.setReviewText(request.getReviewText());
                    return sellerReviewsRepository.save(review);
                })
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    public Optional<SellerReviews> getReviewById(Long reviewId) {
        return sellerReviewsRepository.findById(reviewId);
    }

    public List<SellerReviews> getReviewsBySellerId(Long sellerId) {
        return sellerReviewsRepository.findBySellerId(sellerId);
    }

    public void deleteReview(Long reviewId) {
        sellerReviewsRepository.deleteById(reviewId);
    }

    public List<SellerReviews> getReviewsByReviewerId(Long reviewerId) {
        return sellerReviewsRepository.findByReviewerId(reviewerId);
    }

    //find review by product id
    public List<SellerReviews> getReviewsByProductId(Long productId) {
        return sellerReviewsRepository.findAll().stream()
                .filter(review -> review.getProductId().equals(productId))
                .toList();
    }
    public Double getAverageRatingForSeller(Long userId) {
        List<SellerReviews> reviews = sellerReviewsRepository.findBySellerId(userId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream().mapToDouble(SellerReviews::getRating).sum();
        return sum / reviews.size();
    }   
    
}
