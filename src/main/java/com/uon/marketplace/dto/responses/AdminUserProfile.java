package com.uon.marketplace.dto.responses;

import java.util.List;
import com.uon.marketplace.entities.MarketPlaceProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserProfile {
    private MarketPlaceUser userDetails;
    private List<SellerReviewResponse> sellerReviewsGiven;
    private List<SellerReviewResponse> sellerReviewsReceived;
    private List<BuyerReviewResponse> buyerReviewsGiven;
    private List<BuyerReviewResponse> buyerReviewsReceived;
    private List<MarketPlaceProduct> productsListed;
    private List<MarketPlaceProduct> productsPurchased;
    private Integer totalProductsListed;
    private Integer totalProductsPurchased;
    private Integer totalSellerReviewsGiven;
    private Integer totalSellerReviewsReceived;
    private Integer totalBuyerReviewsGiven;
    private Integer totalBuyerReviewsReceived;
    private Double averageSellerRating;
    private Double averageBuyerRating;
}
