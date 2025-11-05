package com.uon.marketplace.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uon.marketplace.dto.requests.AppUserRequest;
import com.uon.marketplace.dto.requests.CreateUserRequest;
import com.uon.marketplace.dto.requests.UserProfileRequest;
import com.uon.marketplace.dto.responses.AdminUserProfile;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.dto.responses.SellerReviewResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.Role;
import com.uon.marketplace.entities.SellerReviews;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.utils.PasswordHashUtil;
import com.uon.marketplace.utils.ResponseMapper;

@Service
public class AdminService {
    @Autowired
    private AppUserService appUserService;

    @Autowired
    private MarketPlaceProductService marketPlaceProductService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private SellerReviewService sellerReviewService;

    @Autowired
    private BuyerReviewService buyerReviewService;

    @Autowired
    private ResponseMapper responseMapper;

    public AppUserResponse createAdmin(CreateUserRequest request) {
        AppUserRequest appUserReq = request.getAppUser();
       
        AppUser appUser = new AppUser();
        appUser.setRole(Role.ADMIN); // Set role to ADMIN enum
        appUser.setPasswordHash(PasswordHashUtil.hashWithMD5(appUserReq.getPassword())); // Hash in real app
        appUser.setStatus("active");
        appUser.setEmail(appUserReq.getEmail());
        appUser.setCreatedAt(java.time.LocalDateTime.now());
        appUser = appUserService.createUser(appUser);

        UserProfileRequest profileReq = request.getUserProfile();
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(appUser.getUserId());
        userProfile.setFirstName(profileReq.getFirstName());
        userProfile.setLastName(profileReq.getLastName());
        userProfile.setPhoneNumber(profileReq.getPhoneNumber());
        userProfile.setProfileImageUrl(profileReq.getProfileImageUrl());
        userProfileService.createProfile(userProfile);
        return new AppUserResponse(appUser, userProfile);
    }

    public AppUserResponse createUser(CreateUserRequest request) {
        AppUserRequest appUserReq = request.getAppUser();
        AppUser appUser = new AppUser();
        appUser.setRole(Role.USER); // Set role to USER enum
        appUser.setPasswordHash(PasswordHashUtil.hashWithMD5(appUserReq.getPassword())); // Hash in real app
        appUser.setStatus("active");
        appUser.setEmail(appUserReq.getEmail());
        appUser.setCreatedAt(java.time.LocalDateTime.now());
        appUser = appUserService.createUser(appUser);

        UserProfileRequest profileReq = request.getUserProfile();
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(appUser.getUserId());
        userProfile.setFirstName(profileReq.getFirstName());
        userProfile.setLastName(profileReq.getLastName());
        userProfile.setPhoneNumber(profileReq.getPhoneNumber());
        userProfile.setProfileImageUrl(profileReq.getProfileImageUrl());
        userProfileService.createProfile(userProfile);
        return new AppUserResponse(appUser, userProfile);
    }

    public String  resetPassword(String email, String newPassword) {
        AppUser appUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        appUser.setPasswordHash(PasswordHashUtil.hashWithMD5(newPassword)); // Hash in real app
        appUser = appUserService.updateUser(appUser.getUserId(), appUser);
        return "Password reset successful for email: " + email + " New Password: " + newPassword;
    }
    public List<AppUserResponse> getAllUsers() {
        List<AppUser> users = appUserService.getAllUsers();
        List<AppUserResponse> responses = new ArrayList<>();
        for (AppUser user : users) {
            UserProfile profile = userProfileService.getProfileById(user.getUserId())
                    .orElse(new UserProfile());
            responses.add(new AppUserResponse(user, profile));
        }
        return responses;
    }
    //ban user by id
    public void banUser(Long userId) {
        AppUser user = appUserService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("banned");
        appUserService.updateUser(userId, user);
    }
    //view all listed products
    public List<MarketPlaceProduct> getAllListedProducts() {
        return marketPlaceProductService.getAllProducts();
    }
    //remove product by id
    public void removeProduct(Long productId) {
        marketPlaceProductService.deleteProduct(productId);
    }
    //unban user by id
    public void unbanUser(Long userId) {
        AppUser user = appUserService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("active");
        appUserService.updateUser(userId, user);
    }
    //get all Seller reviews of user by id
    public List<SellerReviewResponse> getSellerReviewsByUserId(Long userId) {
        // Assuming there's a SellerReviewService to fetch reviews
        List<SellerReviews> reviews = sellerReviewService.getReviewsBySellerId(userId);
        List<SellerReviewResponse> responseList = new ArrayList<>();
        for (SellerReviews review : reviews) {
            responseList.add(responseMapper.converToSellerReviewResponse(review));
        }
        return responseList;
    }
    //get all Buyer reviews of user by id
    public List<com.uon.marketplace.dto.responses.BuyerReviewResponse> getBuyerReviewsByUserId(Long userId) {
        List<com.uon.marketplace.entities.BuyerReviews> reviews = buyerReviewService.getReviewsByBuyerId(userId);
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> responseList = new ArrayList<>();
        for (com.uon.marketplace.entities.BuyerReviews review : reviews) {
            responseList.add(responseMapper.convertToBuyerReviewResponse(review));
        }
        return responseList;
    }

    public AppUser verifyUser(Long userId) {
        // TODO Auto-generated method stub
        AppUser user = appUserService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("active");
        appUserService.updateUser(userId, user);
        return user;
    }
    public AdminUserProfile getUserProfileForAdmin(Long userId){
        AppUser user = appUserService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getProfileById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        List<SellerReviews> sellerReviewsGiven = sellerReviewService.getReviewsByReviewerId(userId);
        List<SellerReviews> sellerReviewsReceived = sellerReviewService.getReviewsBySellerId(userId);
        List<com.uon.marketplace.entities.BuyerReviews> buyerReviewsGiven = buyerReviewService.getReviewsByReviewerId(userId);
        List<com.uon.marketplace.entities.BuyerReviews> buyerReviewsReceived = buyerReviewService.getReviewsByBuyerId(userId);

        SellerReviewResponse sellerReviewsGivenResponse = null;
        SellerReviewResponse sellerReviewsReceivedResponse = null;
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> buyerReviewsGivenResponse = new ArrayList<>();
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> buyerReviewsReceivedResponse = new ArrayList<>();
        List<SellerReviewResponse> sellerReviewsGivenResponseList = new ArrayList<>();
        List<SellerReviewResponse> sellerReviewsReceivedResponseList = new ArrayList<>();

        if (!sellerReviewsGiven.isEmpty()) {
            for (SellerReviews review : sellerReviewsGiven) {
                sellerReviewsGivenResponseList.add(responseMapper.converToSellerReviewResponse(review));
            }
        }
        if (!sellerReviewsReceived.isEmpty()) {
            for (SellerReviews review : sellerReviewsReceived) {
                sellerReviewsReceivedResponseList.add(responseMapper.converToSellerReviewResponse(review));
            }
        }
        if (!buyerReviewsGiven.isEmpty()) {
            for (com.uon.marketplace.entities.BuyerReviews review : buyerReviewsGiven) {
                buyerReviewsGivenResponse.add(responseMapper.convertToBuyerReviewResponse(review));
            }
        }
        if (!buyerReviewsReceived.isEmpty()) {
            for (com.uon.marketplace.entities.BuyerReviews review : buyerReviewsReceived) {
                buyerReviewsReceivedResponse.add(responseMapper.convertToBuyerReviewResponse(review));
            }
        }
        AppUserResponse appUserResponse = new AppUserResponse(user, profile);
        //get all listed products by user
        List<MarketPlaceProduct> productsListed = marketPlaceProductService.getProductsBySellerId(userId);
        //get all purchased products by user
        List<MarketPlaceProduct> productsPurchased = marketPlaceProductService.getProductsByBuyerId(userId);

        AdminUserProfile adminUserProfile = new AdminUserProfile();
        adminUserProfile.setUserDetails(responseMapper.convertAppUserReponseToMarketplaceuser(appUserResponse));
        adminUserProfile.setSellerReviewsGiven( sellerReviewsGivenResponseList);
        adminUserProfile.setSellerReviewsReceived(  sellerReviewsReceivedResponseList);
        adminUserProfile.setBuyerReviewsGiven(buyerReviewsGivenResponse);
        adminUserProfile.setBuyerReviewsReceived(buyerReviewsReceivedResponse);
        adminUserProfile.setTotalProductsListed(productsListed.size());
        adminUserProfile.setTotalProductsPurchased(productsPurchased.size());
        adminUserProfile.setTotalSellerReviewsGiven(sellerReviewsGiven.size());
        adminUserProfile.setTotalSellerReviewsReceived(sellerReviewsReceived.size());
        adminUserProfile.setTotalBuyerReviewsGiven(buyerReviewsGiven.size());
        adminUserProfile.setTotalBuyerReviewsReceived(buyerReviewsReceived.size());
        adminUserProfile.setProductsListed(productsListed);;
        adminUserProfile.setProductsPurchased(productsPurchased);
        adminUserProfile.setAverageSellerRating(sellerReviewService.getAverageRatingForSeller(userId));
        adminUserProfile.setAverageBuyerRating(buyerReviewService.getAverageRatingForBuyer(userId));

        return adminUserProfile;
    }
    
    public AdminUserProfile getUserProfileForAdminByEmail(String email){
        AppUser user = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return getUserProfileForAdmin(user.getUserId());
    }
    
    //delete user by id - permanently remove user and all associated data
    public void deleteUser(Long userId) {
        // Check if user exists first
        appUserService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete all buyer reviews where user is the reviewer or the buyer being reviewed
        List<com.uon.marketplace.entities.BuyerReviews> buyerReviewsAsReviewer = buyerReviewService.getReviewsByReviewerId(userId);
        for (com.uon.marketplace.entities.BuyerReviews review : buyerReviewsAsReviewer) {
            buyerReviewService.deleteReview(review.getReviewId());
        }
        List<com.uon.marketplace.entities.BuyerReviews> buyerReviewsAsBuyer = buyerReviewService.getReviewsByBuyerId(userId);
        for (com.uon.marketplace.entities.BuyerReviews review : buyerReviewsAsBuyer) {
            buyerReviewService.deleteReview(review.getReviewId());
        }
        
        // Delete all seller reviews where user is the reviewer or the seller being reviewed
        List<SellerReviews> sellerReviewsAsReviewer = sellerReviewService.getReviewsByReviewerId(userId);
        for (SellerReviews review : sellerReviewsAsReviewer) {
            sellerReviewService.deleteReview(review.getReviewId());
        }
        List<SellerReviews> sellerReviewsAsSeller = sellerReviewService.getReviewsBySellerId(userId);
        for (SellerReviews review : sellerReviewsAsSeller) {
            sellerReviewService.deleteReview(review.getReviewId());
        }
        
        // Delete all products where user is seller or buyer
        List<MarketPlaceProduct> productsAsSeller = marketPlaceProductService.getProductsBySellerId(userId);
        for (MarketPlaceProduct product : productsAsSeller) {
            marketPlaceProductService.deleteProduct(product.getProductId());
        }
        List<MarketPlaceProduct> productsAsBuyer = marketPlaceProductService.getProductsByBuyerId(userId);
        for (MarketPlaceProduct product : productsAsBuyer) {
            marketPlaceProductService.deleteProduct(product.getProductId());
        }
        
        // Delete user profile
        userProfileService.deleteProfile(userId);
        
        // Finally, delete the user
        appUserService.deleteUser(userId);
    }
}