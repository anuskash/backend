package com.uon.marketplace.utils;

import org.springframework.beans.factory.annotation.Autowired;

import com.uon.marketplace.dto.responses.SellerReviewResponse;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.SellerReviews;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.services.MarketPlaceProductService;
import com.uon.marketplace.services.UserProfileService;

public class ResponseMapper {
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private MarketPlaceProductService marketPlaceProductService;

      public com.uon.marketplace.dto.responses.BuyerReviewResponse convertToBuyerReviewResponse(com.uon.marketplace.entities.BuyerReviews review) {
        UserProfile reviewerProfile = userProfileService.getProfileByUserId(review.getReviewerId());
        UserProfile buyerProfile = userProfileService.getProfileByUserId(review.getBuyerId());
        com.uon.marketplace.dto.responses.BuyerReviewResponse response = new com.uon.marketplace.dto.responses.BuyerReviewResponse();
        response.setBuyerName(buyerProfile.getFirstName() + " " + buyerProfile.getLastName());
        response.setReviewerName(reviewerProfile.getFirstName() + " " + reviewerProfile.getLastName());
        response.setRating(review.getRating());
        response.setReviewText(review.getReviewText());
        MarketPlaceProduct product = marketPlaceProductService.getProductById(review.getProductId()).orElse(null);
        if(product != null) {
            response.setProductName(product.getProductName());
            response.setProductPrice(product.getPrice());
            response.setProductImageUrl(product.getProductImageUrl());
            response.setCategory(product.getCategory());
            response.setCondition(product.getCondition());
        }
        return response;
    }
    public SellerReviewResponse converToSellerReviewResponse(SellerReviews review) {
        UserProfile reviewerProfile = userProfileService.getProfileByUserId(review.getReviewerId());
        UserProfile sellerProfile = userProfileService.getProfileByUserId(review.getSellerId());
        SellerReviewResponse response = new SellerReviewResponse();
        response.setSellerName(sellerProfile.getFirstName() + " " + sellerProfile.getLastName());
        response.setReviewerName(reviewerProfile.getFirstName() + " " + reviewerProfile.getLastName());
        response.setRating(review.getRating());
        response.setReviewText(review.getReviewText());
        MarketPlaceProduct product = marketPlaceProductService.getProductById(review.getProductId()).orElse(null);
        if(product != null) {
            response.setProductName(product.getProductName());
            response.setProductPrice(product.getPrice());
            response.setProductImageUrl(product.getProductImageUrl());
            response.setCategory(product.getCategory());
            response.setCondition(product.getCondition());
        }
        return response;
    }
    
}
