package com.uon.marketplace.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyerReviewResponse {
    private String buyerName;
    private String reviewerName;
    private Integer rating;
    private String reviewText;
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private String category;
    private String condition;
}
