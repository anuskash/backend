package com.uon.marketplace.utils;

import org.springframework.beans.factory.annotation.Autowired;

import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.dto.responses.MarketPlaceUser;
import com.uon.marketplace.dto.responses.MarketPlaceProductResponse;
import com.uon.marketplace.dto.responses.SellerReviewResponse;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.SellerReviews;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.services.MarketPlaceProductService;
import com.uon.marketplace.services.UserProfileService;

import org.springframework.stereotype.Component;

@Component
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
        response.setReviewId(review.getReviewId());
        response.setReviewerId(review.getReviewerId());
        response.setBuyerId(review.getBuyerId());
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
        response.setReviewId(review.getReviewId());
        response.setSellerName(sellerProfile.getFirstName() + " " + sellerProfile.getLastName());
        response.setReviewerName(reviewerProfile.getFirstName() + " " + reviewerProfile.getLastName());
        response.setRating(review.getRating());
        response.setReviewText(review.getReviewText());
        response.setReviewerId(review.getReviewerId());
        response.setSellerId(review.getSellerId());
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

    public MarketPlaceUser convertAppUserReponseToMarketplaceuser(AppUserResponse appUserResponse) {
        MarketPlaceUser marketPlaceUser = new MarketPlaceUser();
        marketPlaceUser.setUserId(appUserResponse.getAppUser().getUserId());
       marketPlaceUser.setFirstName(appUserResponse.getUserProfile().getFirstName());
         marketPlaceUser.setLastName(appUserResponse.getUserProfile().getLastName());
            marketPlaceUser.setEmail(appUserResponse.getAppUser().getEmail());
            marketPlaceUser.setPhoneNumber(appUserResponse.getUserProfile().getPhoneNumber());
            marketPlaceUser.setRole(appUserResponse.getAppUser().getRole() != null ? appUserResponse.getAppUser().getRole().name() : null);
            marketPlaceUser.setStatus(appUserResponse.getAppUser().getStatus());
            marketPlaceUser.setEmailVerified(appUserResponse.getAppUser().getEmailVerified());
            marketPlaceUser.setTwoFactorEnabled(appUserResponse.getAppUser().getTwoFactorEnabled());
        return marketPlaceUser;
    }
    
    public MarketPlaceProductResponse toMarketPlaceProductResponse(MarketPlaceProduct product) {
        if (product == null) return null;
        MarketPlaceProductResponse response = new MarketPlaceProductResponse();
        response.setProductId(product.getProductId());
        response.setSellerId(product.getSellerId());
        response.setSellerName(product.getSellerName());
        response.setBuyerId(product.getBuyerId());
        response.setBuyerName(product.getBuyerName());
        response.setProductName(product.getProductName());
        response.setCategory(product.getCategory());
        response.setCondition(product.getCondition());
        response.setProductDescription(product.getProductDescription());
        response.setProductImageUrl(product.getProductImageUrl());
        response.setPrice(product.getPrice());
        response.setPostedDate(product.getPostedDate());
        response.setLastUpdate(product.getLastUpdate());
        response.setStatus(product.getStatus());
        response.setFlagged(product.getFlagged());
        response.setFlagReason(product.getFlagReason());
        response.setReportCount(product.getReportCount());
        // collect image URLs from product_images table
        java.util.List<com.uon.marketplace.entities.ProductImage> images = marketPlaceProductService.getProductImages(product.getProductId());
        java.util.List<String> imageUrls = new java.util.ArrayList<>();
        for (com.uon.marketplace.entities.ProductImage img : images) {
            imageUrls.add(img.getImageUrl());
        }
        response.setImageUrls(imageUrls);
        return response;
    }
    
    public java.util.List<MarketPlaceProductResponse> toMarketPlaceProductResponseList(java.util.List<MarketPlaceProduct> products) {
        java.util.List<MarketPlaceProductResponse> list = new java.util.ArrayList<>();
        for (MarketPlaceProduct p : products) {
            list.add(toMarketPlaceProductResponse(p));
        }
        return list;
    }
    
}
