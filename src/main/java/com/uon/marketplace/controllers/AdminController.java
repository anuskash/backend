package com.uon.marketplace.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uon.marketplace.dto.responses.AdminUserProfile;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.entities.AppUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Administrative operations for managing users, reviews, and products")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')") // All admin endpoints require at least ADMIN role
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
    @PreAuthorize("hasRole('SUPER_ADMIN')") // Only super admins can create other admins
    @org.springframework.web.bind.annotation.PostMapping("/create-admin")
    @Operation(summary = "Create admin user", description = "Super admin only - Creates a new admin user in the system")
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
    //verify user endpoint
    @PutMapping("/verify-user/{userId}")
    public org.springframework.http.ResponseEntity<AppUser> verifyUser(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        AppUser verifiedUser = adminService.verifyUser(userId);
        return org.springframework.http.ResponseEntity.ok(verifiedUser);
    }
    //get user profile for admin endpoint
    @org.springframework.web.bind.annotation.GetMapping("/user-profile/{userId}")
    public org.springframework.http.ResponseEntity<AdminUserProfile> getUserProfileForAdmin(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        AdminUserProfile userProfile = adminService.getUserProfileForAdmin(userId);
        return org.springframework.http.ResponseEntity.ok(userProfile);
    }
    
    // get user profile for admin by email (convenience when ID isn't known)
    @Operation(summary = "Get user profile by email", description = "Admin endpoint to fetch complete user profile including status, 2FA settings, reviews, and products by email address")
    @org.springframework.web.bind.annotation.GetMapping("/user-profile/by-email")
    public org.springframework.http.ResponseEntity<AdminUserProfile> getUserProfileForAdminByEmail(@org.springframework.web.bind.annotation.RequestParam String email) {
        AdminUserProfile userProfile = adminService.getUserProfileForAdminByEmail(email.trim().toLowerCase());
        return org.springframework.http.ResponseEntity.ok(userProfile);
    }
    
    //delete user endpoint
    @PreAuthorize("hasRole('SUPER_ADMIN')") // Only super admins can permanently delete users
    @org.springframework.web.bind.annotation.DeleteMapping("/delete-user/{userId}")
    @Operation(summary = "Delete user permanently", description = "Super admin only - Permanently deletes a user from the system")
    public org.springframework.http.ResponseEntity<String> deleteUser(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        adminService.deleteUser(userId);
        return org.springframework.http.ResponseEntity.ok("User with ID " + userId + " has been permanently deleted.");
    }
}