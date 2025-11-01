package com.uon.marketplace.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.Mark;
import com.uon.marketplace.dto.requests.AppUserRequest;
import com.uon.marketplace.dto.requests.CreateUserRequest;
import com.uon.marketplace.dto.requests.UserProfileRequest;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.dto.responses.SellerReviewResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.MarketPlaceProduct;
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

    public AppUserResponse createAdmin(CreateUserRequest request) {
        AppUserRequest appUserReq = request.getAppUser();
       
        AppUser appUser = new AppUser();
    appUser.setRole("admin");
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
    appUser.setRole("user");
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
            responseList.add(new ResponseMapper().converToSellerReviewResponse(review));
        }
        return responseList;
    }
    //get all Buyer reviews of user by id
    public List<com.uon.marketplace.dto.responses.BuyerReviewResponse> getBuyerReviewsByUserId(Long userId) {
        List<com.uon.marketplace.entities.BuyerReviews> reviews = buyerReviewService.getReviewsByBuyerId(userId);
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> responseList = new ArrayList<>();
        for (com.uon.marketplace.entities.BuyerReviews review : reviews) {
            responseList.add(new ResponseMapper().convertToBuyerReviewResponse(review));
        }
        return responseList;
    }
}