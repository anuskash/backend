package com.uon.marketplace.dto.responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyReviews {
    private List<SellerReviewResponse> sellerReviews;
    private List<BuyerReviewResponse> buyerReviews;
    private Long userId;
    private int totalReviews;
    private Double averageBuyerRatingReceived;
    private Double averageSellerRatingReceived;
    private int totalReviewsGiven;
    private int totalReviewsReceived;
    
}
    