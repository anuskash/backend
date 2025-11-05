package com.uon.marketplace.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uon.marketplace.dto.requests.MarketPlaceProductRequest;
import com.uon.marketplace.dto.requests.SellerReviewRequest;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.dto.responses.AverageRating;
import com.uon.marketplace.dto.responses.MarketPlaceUser;
import com.uon.marketplace.dto.responses.MyReviews;
import com.uon.marketplace.dto.responses.ProductReviews;
import com.uon.marketplace.dto.responses.SellerReviewResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.SellerReviews;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.utils.PasswordHashUtil;
import com.uon.marketplace.utils.ResponseMapper;

@Service
public class UserService {
    private final UserProfileService userProfileService;
    private final MarketPlaceProductService marketPlaceProductService;
    private final SellerReviewService sellerReviewService;
    private final BuyerReviewService buyerReviewService;
    private final AppUserService appUserService;
    private final ResponseMapper responseMapper;

    public UserService(UserProfileService userProfileService, MarketPlaceProductService marketPlaceProductService, SellerReviewService sellerReviewService, BuyerReviewService buyerReviewService, AppUserService appUserService, ResponseMapper responseMapper) {
        this.userProfileService = userProfileService;
        this.marketPlaceProductService = marketPlaceProductService;
        this.sellerReviewService = sellerReviewService;
        this.buyerReviewService = buyerReviewService;
        this.appUserService = appUserService;
        this.responseMapper = responseMapper;
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
        product.setCondition(request.getCondition());
        UserProfile sellerProfile = userProfileService.getProfileByUserId(request.getSellerId());
        product.setSellerName(sellerProfile.getFirstName() + " " + sellerProfile.getLastName());
        product.setPrice(request.getPrice());
        product.setStatus("Available");
        product.setCategory(request.getCategory());
        
            // Set the first image as the primary product image URL (for backward compatibility)
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                product.setProductImageUrl(request.getImageUrls().get(0));
            }
        
            // Save the product first
            MarketPlaceProduct savedProduct = marketPlaceProductService.createProduct(product);
        
            // Save all images to the product_images table
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                marketPlaceProductService.saveProductImages(savedProduct.getProductId(), request.getImageUrls());
            }
        
            return savedProduct;
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

    public List<com.uon.marketplace.entities.ProductImage> getProductImages(Long productId) {
        return marketPlaceProductService.getProductImages(productId);
    }

    public void updateProductImages(Long productId, List<String> imageUrls) {
        marketPlaceProductService.saveProductImages(productId, imageUrls);
    }

    public void deleteProductImageByUrl(String imageUrl) {
        marketPlaceProductService.deleteImageByUrl(imageUrl);
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
        return responseMapper.converToSellerReviewResponse(review);
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
        SellerReviews updatedReview = sellerReviewService.updateReview(reviewId, reviewDetails);
        return converToSellerReviewResponse(updatedReview);
    }
        // --- Buyer Review Methods ---
        public com.uon.marketplace.dto.responses.BuyerReviewResponse convertToBuyerReviewResponse(com.uon.marketplace.entities.BuyerReviews review) {
                return responseMapper.convertToBuyerReviewResponse(review);
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
        if(!verifyPurchase(review.getBuyerId(), review.getReviewerId(), review.getProductId())) {
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
        com.uon.marketplace.entities.BuyerReviews updatedReview = buyerReviewService.updateReview(reviewId, reviewDetails);
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
    
    }

    public List<MarketPlaceUser> getAllUsers() {
        List<AppUser> appUserResponses = appUserService.getAllUsers();
        List<AppUserResponse> appUserResponseDtos = new java.util.ArrayList<>();
        for (AppUser appUser : appUserResponses) {
            UserProfile profile = userProfileService.getProfileById(appUser.getUserId())
                    .orElse(new UserProfile());
            appUserResponseDtos.add(new AppUserResponse(appUser, profile));
        }
        List<MarketPlaceUser> marketPlaceUsers = new java.util.ArrayList<>();
        for (AppUserResponse appUserResponse : appUserResponseDtos) {
            marketPlaceUsers.add(responseMapper.convertAppUserReponseToMarketplaceuser(appUserResponse));
        }
        return marketPlaceUsers;
    }
    //get rebviews for a product
    public ProductReviews getReviewsForProduct(Long productId) {
        List<com.uon.marketplace.entities.BuyerReviews> buyerReviewsGiven = buyerReviewService.getReviewsByProductId(productId);
        List<SellerReviews> sellerReviews = sellerReviewService.getReviewsByProductId(productId);
        
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> buyerReviewResponses = new java.util.ArrayList<>();
        for(com.uon.marketplace.entities.BuyerReviews review : buyerReviewsGiven) {
            buyerReviewResponses.add(convertToBuyerReviewResponse(review));
        }
        
        List<SellerReviewResponse> sellerReviewResponses = new java.util.ArrayList<>();
        for(SellerReviews review : sellerReviews) {
            sellerReviewResponses.add(converToSellerReviewResponse(review));
        }
        
        ProductReviews productReviews = new ProductReviews();
        productReviews.setProductId(productId);
        productReviews.setBuyerReviews(buyerReviewResponses);
        productReviews.setSellerReviews(sellerReviewResponses);
        
        return productReviews;
    }
    public MyReviews getMyReviews(Long userId) {

        //recied reviews
        List<SellerReviews> sellerReviews = sellerReviewService.getReviewsBySellerId(userId);
        List<com.uon.marketplace.entities.BuyerReviews> buyerReviews = buyerReviewService.getReviewsByReviewerId(userId);
        System.out.println("Seller Reviews Count: " + sellerReviews.size());
        System.out.println("Buyer Reviews Count: " + buyerReviews.size());
        List<SellerReviewResponse> sellerReviewResponses = new java.util.ArrayList<>();
        for(SellerReviews review : sellerReviews) {
            sellerReviewResponses.add(converToSellerReviewResponse(review));
        }

        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> buyerReviewResponses = new java.util.ArrayList<>();
        for(com.uon.marketplace.entities.BuyerReviews review : buyerReviews) {
            buyerReviewResponses.add(convertToBuyerReviewResponse(review));
        }
        //received reviews
        List<SellerReviews> sellerReviewsGiven = sellerReviewService.getReviewsByReviewerId(userId);
        List<com.uon.marketplace.entities.BuyerReviews> buyerReviewsRecieved = buyerReviewService.getReviewsByBuyerId(userId);
        // append these two lists to the existing lists
        for(SellerReviews review : sellerReviewsGiven) {
            sellerReviewResponses.add(converToSellerReviewResponse(review));
        }
        for(com.uon.marketplace.entities.BuyerReviews review : buyerReviewsRecieved) {
            buyerReviewResponses.add(convertToBuyerReviewResponse(review));
        }

        MyReviews myReviews = new MyReviews();
        myReviews.setSellerReviews(sellerReviewResponses);
        myReviews.setBuyerReviews(buyerReviewResponses);
        myReviews.setUserId(userId);
        myReviews.setTotalReviews(sellerReviews.size() + buyerReviews.size());
        int totalGiven = buyerReviews.size();
        int totalReceived = sellerReviews.size();
        myReviews.setTotalReviewsGiven(totalGiven);
        myReviews.setTotalReviewsReceived(totalReceived);
        Double averageBuyerRatingReceived = 0.0;
        if(totalReceived > 0) {
            double totalBuyerRating = 0;
            for(com.uon.marketplace.entities.BuyerReviews review :  buyerReviewsRecieved) {
                totalBuyerRating += review.getRating();
            }
            averageBuyerRatingReceived = (totalBuyerRating / totalReceived);
        }      
        myReviews.setAverageBuyerRatingReceived(averageBuyerRatingReceived);
        Double averageSellerRatingReceived = 0.0;
        if(totalGiven > 0) {
            double totalSellerRating = 0;
            for(SellerReviews review : sellerReviews) {
                totalSellerRating += review.getRating();
            }
            averageSellerRatingReceived = (totalSellerRating / totalGiven);
        }
        myReviews.setAverageSellerRatingReceived(averageSellerRatingReceived);
        return myReviews;
    }

    public MarketPlaceUser getSellerInfoByUserId(Long userId) {
        AppUser appUser = appUserService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        UserProfile userProfile = userProfileService.getProfileByUserId(userId);
        AppUserResponse appUserResponse = new AppUserResponse(appUser, userProfile);
        return responseMapper.convertAppUserReponseToMarketplaceuser(appUserResponse);
    }

}