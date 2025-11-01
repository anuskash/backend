package com.uon.marketplace.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerReviewRequest {
    private Long reviewerId;
    private Long sellerId;
    private Integer rating;
    private Long productId;
    private String reviewText;
}
