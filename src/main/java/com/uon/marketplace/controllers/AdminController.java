package com.uon.marketplace.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uon.marketplace.dto.responses.AppUserResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {
	@org.springframework.beans.factory.annotation.Autowired
	private com.uon.marketplace.services.AdminService adminService;

	@org.springframework.web.bind.annotation.PostMapping("/create-user")
	public org.springframework.http.ResponseEntity<AppUserResponse> createUser(@org.springframework.web.bind.annotation.RequestBody com.uon.marketplace.dto.requests.CreateUserRequest request) {
		AppUserResponse userResponse = adminService.createUser(request);
		return org.springframework.http.ResponseEntity.ok(userResponse);
	}
    //reset password endpoint
    @org.springframework.web.bind.annotation.PostMapping("/reset-password")
    public org.springframework.http.ResponseEntity<String> resetPassword(@org.springframework.web.bind.annotation.RequestParam String email, @org.springframework.web.bind.annotation.RequestParam String newPassword) {
        String result = adminService.resetPassword(email, newPassword);
        return org.springframework.http.ResponseEntity.ok(result);
    }

    //view all users endpoint
    @org.springframework.web.bind.annotation.GetMapping("/users")
    public org.springframework.http.ResponseEntity<java.util.List<AppUserResponse>> getAllUsers() {
        List<AppUserResponse> users = adminService.getAllUsers();
        return org.springframework.http.ResponseEntity.ok(users);
    }
    //create admin endpoint
    @org.springframework.web.bind.annotation.PostMapping("/create-admin")
    public org.springframework.http.ResponseEntity<AppUserResponse> createAdmin(@org.springframework.web.bind.annotation.RequestBody com.uon.marketplace.dto.requests.CreateUserRequest request) {
        AppUserResponse adminResponse = adminService.createAdmin(request);
        return org.springframework.http.ResponseEntity.ok(adminResponse);
    }
    //get buyer reviews by user id
    @org.springframework.web.bind.annotation.GetMapping("/buyer-reviews/{userId}")
    public org.springframework.http.ResponseEntity<java.util.List<com.uon.marketplace.dto.responses.BuyerReviewResponse>> getBuyerReviewsByUserId(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        List<com.uon.marketplace.dto.responses.BuyerReviewResponse> reviews = adminService.getBuyerReviewsByUserId(userId);
        return org.springframework.http.ResponseEntity.ok(reviews);
    }
    //get seller reviews by user id
    @org.springframework.web.bind.annotation.GetMapping("/seller-reviews/{userId}")
    public org.springframework.http.ResponseEntity<java.util.List<com.uon.marketplace.dto.responses.SellerReviewResponse>> getSellerReviewsByUserId(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        List<com.uon.marketplace.dto.responses.SellerReviewResponse> reviews = adminService.getSellerReviewsByUserId(userId);
        return org.springframework.http.ResponseEntity.ok(reviews);
    }
    //ban user endpoint
    @org.springframework.web.bind.annotation.PostMapping("/ban-user/{userId}")
    public org.springframework.http.ResponseEntity<String> banUser(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        adminService.banUser(userId);
        return org.springframework.http.ResponseEntity.ok("User with ID " + userId + " has been banned.");
    }
    //unban user endpoint
    @org.springframework.web.bind.annotation.PostMapping("/unban-user/{userId}")
    public org.springframework.http.ResponseEntity<String> unbanUser(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        adminService.unbanUser(userId);
        return org.springframework.http.ResponseEntity.ok("User with ID " + userId + " has been unbanned.");
    }
}