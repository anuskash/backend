package com.uon.marketplace.controllers;

import com.uon.marketplace.dto.requests.MarketPlaceProductRequest;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/profile/{userId}/picture")
    public ResponseEntity<UserProfile> changeProfilePicture(@PathVariable Long userId, @RequestParam String newProfileImageUrl) {
        return ResponseEntity.ok(userService.changeProfilePicture(userId, newProfileImageUrl));
    }

    @PutMapping("/profile/{userId}/phone")
    public ResponseEntity<UserProfile> updatePhoneNumber(@PathVariable Long userId, @RequestParam String newPhoneNumber) {
        return ResponseEntity.ok(userService.updatePhoneNumber(userId, newPhoneNumber));
    }

    @PostMapping("/product")
    public ResponseEntity<MarketPlaceProduct> addMarketPlaceProduct(@RequestBody MarketPlaceProductRequest request) {
        return ResponseEntity.ok(userService.addMarketPlaceProduct(request));
    }

    @PutMapping("/product/{productId}/status")
    public ResponseEntity<MarketPlaceProduct> updateProductStatus(@PathVariable Long productId, @RequestParam String newStatus) {
        return ResponseEntity.ok(userService.updateProductStatus(productId, newStatus));
    }

    @PutMapping("/product/{productId}/sold")
    public ResponseEntity<MarketPlaceProduct> markProductAsSold(@PathVariable Long productId, @RequestParam Long buyerUserId) {
        return ResponseEntity.ok(userService.markProductAsSold(productId, buyerUserId));
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable Long productId) {
        userService.removeProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/product/{productId}/unavailable")
    public ResponseEntity<MarketPlaceProduct> markProductAsUnavailable(@PathVariable Long productId) {
        return ResponseEntity.ok(userService.markProductAsUnavailable(productId));
    }

    @PutMapping("/product/{productId}/price")
    public ResponseEntity<MarketPlaceProduct> updateProductPrice(@PathVariable Long productId, @RequestParam BigDecimal newPrice) {
        return ResponseEntity.ok(userService.updateProductPrice(productId, newPrice));
    }

    @GetMapping("/products/seller/{sellerId}")
    public ResponseEntity<List<MarketPlaceProduct>> getProductsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(userService.getProductsBySeller(sellerId));
    }

    @GetMapping("/products/buyer/{buyerId}")
    public ResponseEntity<List<MarketPlaceProduct>> getProductsByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(userService.getProductsByBuyer(buyerId));
    }
    @GetMapping("/products/available")
    public ResponseEntity<List<MarketPlaceProduct>> getAllAvailableProducts() {
        return ResponseEntity.ok(userService.getAllAvailableProducts());
    }
    // --- Seller Review Endpoints ---

    @GetMapping("/reviews/seller/{sellerId}")
    public ResponseEntity<List<com.uon.marketplace.dto.responses.SellerReviewResponse>> getAllReviewsBySellerId(@PathVariable Long sellerId) {
        return ResponseEntity.ok(userService.getAllReviewsBySellerId(sellerId));
    }

    @GetMapping("/reviews/reviewer/{reviewerId}")
    public ResponseEntity<List<com.uon.marketplace.dto.responses.SellerReviewResponse>> getAllReviewsByReviewerId(@PathVariable Long reviewerId) {
        return ResponseEntity.ok(userService.getAllReviewsByReviewerId(reviewerId));
    }

    @PostMapping("/reviews/seller")
    public ResponseEntity<com.uon.marketplace.dto.responses.SellerReviewResponse> addSellerReview(@RequestBody com.uon.marketplace.dto.requests.SellerReviewRequest review) {
        return ResponseEntity.ok(userService.addSellerReview(review));
    }

    @PutMapping("/reviews/seller/{reviewId}")
    public ResponseEntity<com.uon.marketplace.dto.responses.SellerReviewResponse> updateReview(@PathVariable Long reviewId, @RequestBody com.uon.marketplace.dto.requests.SellerReviewRequest reviewDetails) {
        return ResponseEntity.ok(userService.updatReviewResponse(reviewId, reviewDetails));
    }
    // --- Buyer Review Endpoints ---

    @GetMapping("/buyer-reviews/buyer/{buyerId}")
    public ResponseEntity<List<com.uon.marketplace.dto.responses.BuyerReviewResponse>> getAllReviewsByBuyerId(@PathVariable Long buyerId) {
        return ResponseEntity.ok(userService.getAllReviewsByBuyerId(buyerId));
    }

    @GetMapping("/buyer-reviews/reviewer/{reviewerId}")
    public ResponseEntity<List<com.uon.marketplace.dto.responses.BuyerReviewResponse>> getAllReviewsByReviewerIdForBuyer(@PathVariable Long reviewerId) {
        return ResponseEntity.ok(userService.getAllReviewsByReviewerIdForBuyer(reviewerId));
    }

    @PostMapping("/buyer-reviews")
    public ResponseEntity<com.uon.marketplace.dto.responses.BuyerReviewResponse> addBuyerReview(@RequestBody com.uon.marketplace.dto.requests.BuyerReviewRequest review) {
        return ResponseEntity.ok(userService.addBuyerReview(review));
    }

    @PutMapping("/buyer-reviews/{reviewId}")
    public ResponseEntity<com.uon.marketplace.dto.responses.BuyerReviewResponse> updateBuyerReview(@PathVariable Long reviewId, @RequestBody com.uon.marketplace.dto.requests.BuyerReviewRequest reviewDetails) {
        return ResponseEntity.ok(userService.updateBuyerReview(reviewId, reviewDetails));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam Long userId, @RequestParam String newPassword) {
        String result = userService.resetPassword(userId, newPassword);
        return ResponseEntity.ok(result);
    }   
}