package com.uon.marketplace.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import com.uon.marketplace.dto.requests.MarketPlaceProductRequest;
import com.uon.marketplace.dto.responses.MarketPlaceUser;
import com.uon.marketplace.dto.requests.ProductImagesUpdateRequest;
import com.uon.marketplace.dto.responses.MyReviews;
import com.uon.marketplace.dto.responses.ProductReviews;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.entities.SavedProduct;
import com.uon.marketplace.services.ImageUploadService;
import com.uon.marketplace.services.UserService;
import com.uon.marketplace.services.SavedProductService;
import com.uon.marketplace.services.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final ImageUploadService imageUploadService;
    private final SavedProductService savedProductService;
    private final NotificationService notificationService;

    public UserController(UserService userService, ImageUploadService imageUploadService, SavedProductService savedProductService, NotificationService notificationService) {
        this.userService = userService;
        this.imageUploadService = imageUploadService;
        this.savedProductService = savedProductService;
        this.notificationService = notificationService;
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
    public ResponseEntity<?> updateProductStatus(@PathVariable Long productId, 
                                                 @RequestParam String newStatus,
                                                 @RequestHeader("userId") Long userId) {
        try {
            MarketPlaceProduct product = userService.getProductById(productId);
            if (!product.getSellerId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Unauthorized: You can only modify your own products");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.ok(userService.updateProductStatus(productId, newStatus));
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/product/{productId}/sold")
    public ResponseEntity<?> markProductAsSold(@PathVariable Long productId, 
                                               @RequestParam Long buyerUserId,
                                               @RequestHeader("userId") Long userId) {
        try {
            MarketPlaceProduct product = userService.getProductById(productId);
            if (!product.getSellerId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Unauthorized: You can only mark your own products as sold");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.ok(userService.markProductAsSold(productId, buyerUserId));
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<?> removeProduct(@PathVariable Long productId,
                                          @RequestHeader("userId") Long userId) {
        try {
            MarketPlaceProduct product = userService.getProductById(productId);
            if (!product.getSellerId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Unauthorized: You can only delete your own products");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            userService.removeProduct(productId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/product/{productId}/unavailable")
    public ResponseEntity<?> markProductAsUnavailable(@PathVariable Long productId,
                                                      @RequestHeader("userId") Long userId) {
        try {
            MarketPlaceProduct product = userService.getProductById(productId);
            if (!product.getSellerId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Unauthorized: You can only modify your own products");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.ok(userService.markProductAsUnavailable(productId));
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/product/{productId}/price")
    public ResponseEntity<?> updateProductPrice(@PathVariable Long productId, 
                                               @RequestParam BigDecimal newPrice,
                                               @RequestHeader("userId") Long userId) {
        try {
            MarketPlaceProduct product = userService.getProductById(productId);
            if (!product.getSellerId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Unauthorized: You can only modify your own products");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.ok(userService.updateProductPrice(productId, newPrice));
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/product/{productId}")
    @Operation(summary = "Update product details", 
               description = "Update product name, description, category, condition, price, and images. Only provided fields will be updated.")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, 
                                          @RequestBody com.uon.marketplace.dto.requests.UpdateProductRequest request,
                                          @RequestHeader("userId") Long userId) {
        try {
            // Verify seller ownership
            MarketPlaceProduct product = userService.getProductById(productId);
            if (!product.getSellerId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Unauthorized: You can only edit your own products");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            return ResponseEntity.ok(userService.updateProduct(productId, request));
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/products/seller/{sellerId}")
    public ResponseEntity<java.util.List<com.uon.marketplace.dto.responses.MarketPlaceProductResponse>> getProductsBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(userService.getDetailedProductsBySeller(sellerId));
    }

    @GetMapping("/products/buyer/{buyerId}")
    public ResponseEntity<java.util.List<com.uon.marketplace.dto.responses.MarketPlaceProductResponse>> getProductsByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(userService.getDetailedProductsByBuyer(buyerId));
    }
    @GetMapping("/products/available")
    public ResponseEntity<java.util.List<com.uon.marketplace.dto.responses.MarketPlaceProductResponse>> getAllAvailableProducts() {
        return ResponseEntity.ok(userService.getAllDetailedAvailableProducts());
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
    @Operation(
        summary = "Upload multiple product images",
        description = "Upload up to 10 images for a product listing. Accepts JPEG, PNG, WEBP. Max size per file: 5MB"
    )
    @ApiResponse(responseCode = "200", description = "Images uploaded successfully")
    @PostMapping(value = "/product/upload-multiple-images", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadMultipleProductImages(
        @io.swagger.v3.oas.annotations.Parameter(
            description = "Array of image files",
            required = true,
            content = @Content(
                mediaType = "multipart/form-data",
                array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary")
                )
            )
        )
        @RequestPart("files") MultipartFile[] files) {
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
            // Also remove DB record for this image URL, if any
            userService.deleteProductImageByUrl(imageUrl);
            // optional: notify seller about image deletion if needed later
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to delete image");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all images for a product
     * @param productId the ID of the product
     * @return list of image URLs
     */
    @Operation(summary = "Get all images for a product",
               description = "Retrieve all uploaded images for a specific product, ordered by display order")
    @GetMapping("/product/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Long productId) {
        List<com.uon.marketplace.entities.ProductImage> images = userService.getProductImages(productId);
        List<String> imageUrls = images.stream()
                .map(com.uon.marketplace.entities.ProductImage::getImageUrl)
                .collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("imageUrls", imageUrls);
        response.put("count", imageUrls.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Update product images list and order
     */
    @Operation(summary = "Update product images",
               description = "Attach or reorder images for an existing product. First URL becomes primary.")
    @PutMapping("/product/{productId}/images")
    public ResponseEntity<?> updateProductImages(@PathVariable Long productId,
                                                 @RequestBody ProductImagesUpdateRequest request,
                                                                                                  @RequestHeader("userId") Long userId) {
        try {
            MarketPlaceProduct product = userService.getProductById(productId);
            if (!product.getSellerId().equals(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Unauthorized: You can only modify your own products");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            userService.updateProductImages(productId, request.getImageUrls());
            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("updated", true);
            response.put("count", request.getImageUrls() != null ? request.getImageUrls().size() : 0);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Submit a product report
     */
    @PostMapping("/reports/product")
    @Operation(summary = "Report a product", description = "Submit a report for a product with reason and details")
    public ResponseEntity<?> reportProduct(@RequestBody com.uon.marketplace.dto.requests.ProductReportRequest request,
                                          @RequestHeader("userId") Long reporterId) {
        try {
            // Validate product exists
            MarketPlaceProduct product = userService.getProductById(request.getProductId());
            if (product == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Product not found");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if user already reported this product
            if (userService.hasUserReportedProduct(request.getProductId(), reporterId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You have already reported this product");
                return ResponseEntity.badRequest().body(response);
            }

            // Create report
            com.uon.marketplace.entities.ProductReport report = new com.uon.marketplace.entities.ProductReport();
            report.setProductId(request.getProductId());
            report.setReporterId(reporterId);
            report.setReportReason(request.getReportReason());
            report.setReportDetails(request.getReportDetails());
            report.setReportDate(java.time.LocalDateTime.now());
            report.setStatus("pending");

            userService.saveProductReport(report);

            // Increment report count on product (null-safe)
            product.incrementReportCount();
            
            // Auto-flag product if it reaches threshold (e.g., 3 reports)
            if (product.getReportCount() >= 3 && Boolean.FALSE.equals(product.getFlagged())) {
                product.setFlagged(true);
                product.setFlagReason("Multiple user reports (" + product.getReportCount() + ")");
                try {
                    notificationService.create(
                        product.getSellerId(),
                        "PRODUCT_FLAGGED",
                        "Product Flagged for Review: " + product.getProductName(),
                        "Your product was automatically flagged for review due to multiple user reports (" + product.getReportCount() + ").",
                        true
                    );
                } catch (Exception ex) {
                    // non-fatal for reporting flow
                }
            }
            
            userService.saveProduct(product);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product reported successfully");
            response.put("reportId", report.getReportId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to submit report: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== SAVED PRODUCTS ENDPOINTS ====================

    /**
     * Save a product to user's saved list
     */
    @PostMapping("/saved-products")
    @Operation(summary = "Save product", description = "Add a product to user's saved/favorites list")
    public ResponseEntity<?> saveProduct(@RequestParam Long productId,
                                        @RequestHeader("userId") Long userId) {
        try {
            SavedProduct saved = savedProductService.saveProduct(userId, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product saved successfully");
            response.put("savedId", saved.getSavedId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Remove a product from user's saved list
     */
    @DeleteMapping("/saved-products/{productId}")
    @Operation(summary = "Unsave product", description = "Remove a product from user's saved/favorites list")
    public ResponseEntity<?> unsaveProduct(@PathVariable Long productId,
                                          @RequestHeader("userId") Long userId) {
        try {
            savedProductService.unsaveProduct(userId, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product removed from saved list");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to unsave product: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get user's saved products list
     */
    @GetMapping("/saved-products")
    @Operation(summary = "Get saved products", description = "Retrieve user's saved/favorites product list")
    public ResponseEntity<?> getSavedProducts(@RequestHeader("userId") Long userId) {
        try {
            List<SavedProduct> savedProducts = savedProductService.getSavedProductsByUser(userId);
            List<com.uon.marketplace.dto.responses.SavedProductWithImageResponse> responseList = new java.util.ArrayList<>();
            for (SavedProduct saved : savedProducts) {
                String productImageUrl = null;
                try {
                    com.uon.marketplace.entities.MarketPlaceProduct product = userService.getProductById(saved.getProductId());
                    productImageUrl = product != null ? product.getProductImageUrl() : null;
                } catch (Exception ex) {
                    productImageUrl = null;
                }
                responseList.add(new com.uon.marketplace.dto.responses.SavedProductWithImageResponse(
                    saved.getSavedId(),
                    saved.getUserId(),
                    saved.getProductId(),
                    saved.getSavedDate(),
                    productImageUrl
                ));
            }
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch saved products: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Check if a product is saved by user
     */
    @GetMapping("/saved-products/check/{productId}")
    @Operation(summary = "Check if product is saved", description = "Check if a specific product is in user's saved list")
    public ResponseEntity<?> isProductSaved(@PathVariable Long productId,
                                           @RequestHeader("userId") Long userId) {
        try {
            boolean isSaved = savedProductService.isProductSaved(userId, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("isSaved", isSaved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to check saved status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== NOTIFICATIONS ENDPOINTS ====================

    /**
     * Get user's notifications
     */
    @GetMapping("/notifications")
    @Operation(summary = "List notifications", description = "Retrieve user's notifications ordered by newest first")
    public ResponseEntity<?> getNotifications(@RequestHeader("userId") Long userId) {
        try {
            return ResponseEntity.ok(notificationService.list(userId));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch notifications: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get count of unread notifications
     */
    @GetMapping("/notifications/unread-count")
    @Operation(summary = "Unread count", description = "Count user's unread notifications")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("userId") Long userId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("count", notificationService.unreadCount(userId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to count unread notifications: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Mark single notification as read
     */
    @PostMapping("/notifications/{notificationId}/read")
    @Operation(summary = "Mark notification read", description = "Mark a specific notification as read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long notificationId,
                                                   @RequestHeader("userId") Long userId) {
        try {
            notificationService.markRead(notificationId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/notifications/read-all")
    @Operation(summary = "Mark all read", description = "Mark all user's notifications as read")
    public ResponseEntity<?> markAllNotificationsRead(@RequestHeader("userId") Long userId) {
        try {
            int updated = notificationService.markAllRead(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All notifications marked as read");
            response.put("updated", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to mark all read: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}
