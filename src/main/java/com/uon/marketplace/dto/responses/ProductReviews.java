package com.uon.marketplace.dto.responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviews {
    private Long productId;
    private List<BuyerReviewResponse> buyerReviews;
    private List<SellerReviewResponse> sellerReviews;

}
