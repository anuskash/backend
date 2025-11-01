package com.uon.marketplace.dto.responses;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerReviewResponse {
    private String reviewerName;
    private String reviewText;
    private int rating;
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private String sellerName;
    private String category;
    private String condition;

    
}
