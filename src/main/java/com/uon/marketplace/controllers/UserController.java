package com.uon.marketplace.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.uon.marketplace.dto.requests.MarketPlaceProductRequest;
import com.uon.marketplace.dto.responses.MarketPlaceUser;
import com.uon.marketplace.dto.responses.MyReviews;
import com.uon.marketplace.dto.responses.ProductReviews;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.services.ImageUploadService;
import com.uon.marketplace.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final ImageUploadService imageUploadService;

    @Autowired
    public UserController(UserService userService, ImageUploadService imageUploadService) {
        this.userService = userService;
        this.imageUploadService = imageUploadService;
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

    @PutMapping("/reviews/{reviewId}")
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
    @GetMapping("/all/users")
    public ResponseEntity<List<MarketPlaceUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/product-reviews/{productId}")
    public ResponseEntity<ProductReviews> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(userService.getReviewsForProduct(productId));
    }
    @GetMapping("/my-reviews/{userId}")
    public ResponseEntity<MyReviews> getMyReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getMyReviews(userId));
    }
  // endpoint to get seller info by user id
    @GetMapping("/seller-info/{sellerId}")
    public ResponseEntity<MarketPlaceUser> getSellerInfoByUserId(@PathVariable Long sellerId) {
        return ResponseEntity.ok(userService.getSellerInfoByUserId(sellerId));
    }

    // --- Image Upload Endpoints ---
    
    /**
     * Upload a single product image
     * @param file the image file to upload
     * @return JSON with imageUrl
     */
    @Operation(
        summary = "Upload a single product image",
        description = "Upload an image file for a product listing. Accepts JPEG, PNG, WEBP. Max size: 5MB",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "multipart/form-data")
        )
    )
    @ApiResponse(responseCode = "200", description = "Image uploaded successfully")
    @PostMapping(value = "/product/upload-image", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadProductImage(@RequestPart("file") MultipartFile file) {
        try {
            String imageUrl = imageUploadService.uploadImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Upload multiple product images
     * @param files array of image files to upload
     * @return JSON with list of imageUrls
     */
    @Operation(summary = "Upload multiple product images",
               description = "Upload up to 10 images for a product listing. Accepts JPEG, PNG, WEBP. Max size per file: 5MB")
    @ApiResponse(responseCode = "200", description = "Images uploaded successfully")
    @PostMapping(value = "/product/upload-multiple-images", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadMultipleProductImages(@RequestPart("files") MultipartFile[] files) {
        try {
            List<String> imageUrls = imageUploadService.uploadMultipleImages(files);
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrls", imageUrls);
            response.put("message", "Images uploaded successfully");
            response.put("count", imageUrls.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a product image
     * @param imageUrl the URL of the image to delete
     * @return success message
     */
    @DeleteMapping("/product/delete-image")
    public ResponseEntity<?> deleteProductImage(@RequestParam String imageUrl) {
        boolean deleted = imageUploadService.deleteImage(imageUrl);
        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to delete image");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}