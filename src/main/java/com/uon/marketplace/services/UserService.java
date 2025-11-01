package com.uon.marketplace.services;

import java.util.List;

import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import com.uon.marketplace.dto.requests.MarketPlaceProductRequest;
import com.uon.marketplace.dto.requests.SellerReviewRequest;
import com.uon.marketplace.dto.responses.AverageRating;
import com.uon.marketplace.dto.responses.SellerReviewResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.SellerReviews;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.utils.PasswordHashUtil;

@Service
public class UserService {
    private final UserProfileService userProfileService;
    private final MarketPlaceProductService marketPlaceProductService;
    private final SellerReviewService sellerReviewService;
    private final BuyerReviewService buyerReviewService;
    private final AppUserService appUserService;

    public UserService(UserProfileService userProfileService, MarketPlaceProductService marketPlaceProductService, SellerReviewService sellerReviewService, BuyerReviewService buyerReviewService, AppUserService appUserService) {
        this.userProfileService = userProfileService;
        this.marketPlaceProductService = marketPlaceProductService;
        this.sellerReviewService = sellerReviewService;
        this.buyerReviewService = buyerReviewService;
        this.appUserService = appUserService;
    }

    public UserProfile getUserProfile(Long userId) {
        return userProfileService.getProfileByUserId(userId);
    }
    public UserProfile changeProfilePicture(Long userId, String newProfileImageUrl) {
        UserProfile profile = userProfileService.getProfileByUserId(userId);
        if(profile == null) {
            throw new RuntimeException("Profile not found for user ID: " + userId);
        }
        profile.setProfileImageUrl(newProfileImageUrl);
        return userProfileService.updateProfile(profile.getProfileId(), profile);
    }
    public  UserProfile updatePhoneNumber(Long userId, String newPhoneNumber) {
        UserProfile profile = userProfileService.getProfileByUserId(userId);
        if(profile == null) {
            throw new RuntimeException("Profile not found for user ID: " + userId);
        }
        profile.setPhoneNumber(newPhoneNumber);
        return userProfileService.updateProfile(profile.getProfileId(), profile);
    }
    public MarketPlaceProduct addMarketPlaceProduct(MarketPlaceProductRequest request) {
        MarketPlaceProduct product = new MarketPlaceProduct();
        product.setSellerId(request.getSellerId());
        product.setProductName(request.getProductName());
        product.setProductDescription(request.getProductDescription());
        product.setProductImageUrl(request.getProductImageUrl());
        product.setCondition(request.getCondition());
        UserProfile sellerProfile = userProfileService.getProfileByUserId(request.getSellerId());
        product.setSellerName(sellerProfile.getFirstName() + " " + sellerProfile.getLastName());
        product.setPrice(request.getPrice());
        product.setStatus("Available");
        product.setCategory(request.getCategory());
        return marketPlaceProductService.createProduct(product);
    }
    //update product status
    public MarketPlaceProduct updateProductStatus(Long productId, String newStatus) {
        return marketPlaceProductService.updateProductStatus(productId, newStatus);
    }
    // mark as sold
    public MarketPlaceProduct markProductAsSold(Long productId, Long buyerUserId) {
        UserProfile buyUserProfile = userProfileService.getProfileByUserId(buyerUserId);
        return marketPlaceProductService.markProductSold(productId, buyerUserId, buyUserProfile.getFirstName()+buyUserProfile.getLastName());

    }
    //remove product
    public void removeProduct(Long productId) {
        marketPlaceProductService.deleteProduct(productId);
    }
    //mark product unavailable
    public MarketPlaceProduct markProductAsUnavailable(Long productId) {
        return marketPlaceProductService.updateProductStatus(productId, "Unavailable");
    }
    //update product price
    public MarketPlaceProduct updateProductPrice(Long productId, java.math.BigDecimal newPrice) {
        return marketPlaceProductService.updateProductPrice(productId, newPrice);
    }
    //get all products of a seller
    public java.util.List<MarketPlaceProduct> getProductsBySeller(Long sellerId) {
        return marketPlaceProductService.getProductsBySellerId(sellerId);
    }
    //get all product of a buyer
    public java.util.List<MarketPlaceProduct> getProductsByBuyer(Long buyerId) {
        return marketPlaceProductService.getProductsByBuyerId(buyerId);
    }

    //get all available products
    public java.util.List<MarketPlaceProduct> getAllAvailableProducts() {
        return marketPlaceProductService.getAvailableProducts();
    }
    public SellerReviewResponse converToSellerReviewResponse(SellerReviews review) {
        return new com.uon.marketplace.utils.ResponseMapper().converToSellerReviewResponse(review);
    }
    public List<SellerReviewResponse> getAllReviewsBySellerId(Long sellerId) {
        java.util.List<SellerReviews> reviews = sellerReviewService.getReviewsBySellerId(sellerId);
        if(reviews.isEmpty()) {
            throw new RuntimeException("No reviews found for seller ID: " + sellerId);
        }
        List<SellerReviewResponse> responseList = new java.util.ArrayList<>();
        for(SellerReviews review : reviews) {
            responseList.add(converToSellerReviewResponse(review));
        }
        return responseList;
    }
    
    public List<SellerReviewResponse> getAllReviewsByReviewerId(Long reviewerId) {
        java.util.List<SellerReviews> reviews = sellerReviewService.getReviewsByReviewerId(reviewerId);
        if(reviews.isEmpty()) {
            throw new RuntimeException("No reviews found for reviewer ID: " + reviewerId);
        }
        List<SellerReviewResponse> responseList = new java.util.ArrayList<>();
        for(SellerReviews review : reviews) {
            responseList.add(converToSellerReviewResponse(review));
        }
        return responseList;
    }
    public SellerReviewResponse addSellerReview(SellerReviewRequest review) {
        if(!verifyPurchase(review.getReviewerId(), review.getSellerId(), review.getProductId())) {
            throw new RuntimeException("Purchase verification failed. Review cannot be submitted.");
        }
        SellerReviews savedReview = sellerReviewService.createReview(review);
        return converToSellerReviewResponse(savedReview);
    }
    //verify valid purchase before allowing review submission can be handled in controller layer
    public boolean verifyPurchase(Long buyerId, Long sellerId, Long productId) {
        java.util.List<MarketPlaceProduct> products = marketPlaceProductService.getProductsByBuyerId(buyerId);
        for(MarketPlaceProduct product : products) {
            if(product.getSellerId().equals(sellerId) && product.getProductId().equals(productId) && product.getStatus().equals("Sold")) {
                return true;
            }
        }
        return false;
    }
    public SellerReviewResponse updatReviewResponse(Long reviewId, SellerReviewRequest reviewDetails) {
        SellerReviews existingReview = sellerReviewService.getReviewById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        existingReview.setRating(reviewDetails.getRating());
        existingReview.setReviewText(reviewDetails.getReviewText());
        SellerReviews updatedReview = sellerReviewService.createReview(reviewDetails);
        return converToSellerReviewResponse(updatedReview);
    }
        // --- Buyer Review Methods ---
    public com.uon.marketplace.dto.responses.BuyerReviewResponse convertToBuyerReviewResponse(com.uon.marketplace.entities.BuyerReviews review) {
      return new com.uon.marketplace.utils.ResponseMapper().convertToBuyerReviewResponse(review);
    }

    public List<com.uon.marketplace.dto.responses.BuyerReviewResponse> getAllReviewsByBuyerId(Long buyerId) {
        List<com.uon.marketplace.entities.BuyerReviews> reviews = buyerReviewService.getReviewsByBuyerId(buyerId);
        if(reviews.isEmpty()) {
            throw new RuntimeException("No reviews found for buyer ID: " + buyerId);
        }
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> responseList = new java.util.ArrayList<>();
        for(com.uon.marketplace.entities.BuyerReviews review : reviews) {
            responseList.add(convertToBuyerReviewResponse(review));
        }
        return responseList;
    }

    public List<com.uon.marketplace.dto.responses.BuyerReviewResponse> getAllReviewsByReviewerIdForBuyer(Long reviewerId) {
        List<com.uon.marketplace.entities.BuyerReviews> reviews = buyerReviewService.getReviewsByReviewerId(reviewerId);
        if(reviews.isEmpty()) {
            throw new RuntimeException("No reviews found for reviewer ID: " + reviewerId);
        }
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> responseList = new java.util.ArrayList<>();
        for(com.uon.marketplace.entities.BuyerReviews review : reviews) {
            responseList.add(convertToBuyerReviewResponse(review));
        }
        return responseList;
    }

    public com.uon.marketplace.dto.responses.BuyerReviewResponse addBuyerReview(com.uon.marketplace.dto.requests.BuyerReviewRequest review) {
        if(!verifyPurchase(review.getReviewerId(), review.getBuyerId(), review.getProductId())) {
            throw new RuntimeException("Purchase verification failed. Review cannot be submitted.");
        }
        com.uon.marketplace.entities.BuyerReviews savedReview = buyerReviewService.createReview(review);
        return convertToBuyerReviewResponse(savedReview);
    }

    public com.uon.marketplace.dto.responses.BuyerReviewResponse updateBuyerReview(Long reviewId, com.uon.marketplace.dto.requests.BuyerReviewRequest reviewDetails) {
        com.uon.marketplace.entities.BuyerReviews existingReview = buyerReviewService.getReviewById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        existingReview.setRating(reviewDetails.getRating());
        existingReview.setReviewText(reviewDetails.getReviewText());
        existingReview.setProductId(reviewDetails.getProductId());
        existingReview.setBuyerId(reviewDetails.getBuyerId());
        existingReview.setReviewerId(reviewDetails.getReviewerId());
        com.uon.marketplace.entities.BuyerReviews updatedReview = buyerReviewService.createReview(reviewDetails);
        return convertToBuyerReviewResponse(updatedReview);
    }

    public String resetPassword(Long userId, String newPassword) {
        AppUser appUser = appUserService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String hashedPassword = PasswordHashUtil.hashWithMD5(newPassword); // Hash in real app
        appUser.setPasswordHash(hashedPassword);
        appUserService.updateUser(userId, appUser);
        return "Password reset successfully for user ID: " + userId + " New Password: " + newPassword;
    }
    //get average rating for a seller
    public AverageRating getAverageRatingForSeller(Long sellerId) {
        List<SellerReviews> reviews = sellerReviewService.getReviewsBySellerId(sellerId);
        if(reviews.isEmpty()) {
            throw new RuntimeException("No reviews found for seller ID: " + sellerId);
        }
        double totalRating = 0;
        for(SellerReviews review : reviews) {
            totalRating += review.getRating();
        }
        double average = totalRating / reviews.size();
        AverageRating avgRating = new AverageRating();
        avgRating.setAverageRating(average);
        avgRating.setTotalReviews(reviews.size());
        return avgRating;
    }
    //get average rating for a buyer
    public AverageRating getAverageRatingForBuyer(Long buyerId) {
        List<com.uon.marketplace.entities.BuyerReviews> reviews = buyerReviewService.getReviewsByBuyerId(buyerId);
        if(reviews.isEmpty()) {
            throw new RuntimeException("No reviews found for buyer ID: " + buyerId);
        }
        double totalRating = 0;
        for(com.uon.marketplace.entities.BuyerReviews review : reviews) {
            totalRating += review.getRating();
        }
        double average = totalRating / reviews.size();
        AverageRating avgRating = new AverageRating();
        avgRating.setAverageRating(average);
        avgRating.setTotalReviews(reviews.size());
        return avgRating;
    
    }}