package com.uon.marketplace.controllers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

// import org.springframework.security.access.prepost.PreAuthorize; // Removed for development
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uon.marketplace.dto.requests.ProhibitedKeywordRequest;
import com.uon.marketplace.dto.responses.AdminUserProfile;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.ProhibitedKeyword;
import com.uon.marketplace.entities.ProductReport;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.repositories.ProhibitedKeywordRepository;
import com.uon.marketplace.repositories.ProductReportRepository;
import com.uon.marketplace.repositories.MarketPlaceProductRepository;
import com.uon.marketplace.repositories.AppUserRepository;
import com.uon.marketplace.services.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Administrative operations for managing users, reviews, and products")
// @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')") // Removed for easier frontend access during development
public class AdminController {
	@org.springframework.beans.factory.annotation.Autowired
	private com.uon.marketplace.services.AdminService adminService;

	@org.springframework.beans.factory.annotation.Autowired
	private ProhibitedKeywordRepository prohibitedKeywordRepository;

	@org.springframework.beans.factory.annotation.Autowired
	private ProductReportRepository reportRepository;

	@org.springframework.beans.factory.annotation.Autowired
	private MarketPlaceProductRepository productRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private AppUserRepository appUserRepository;

	@org.springframework.beans.factory.annotation.Autowired
	private com.uon.marketplace.repositories.SellerReviewsRepository sellerReviewsRepository;

	@org.springframework.beans.factory.annotation.Autowired
	private com.uon.marketplace.repositories.BuyerReviewRepository buyerReviewRepository;

	@org.springframework.beans.factory.annotation.Autowired
	private NotificationService notificationService;

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
    // @PreAuthorize("hasRole('SUPER_ADMIN')") // Removed for easier frontend access during development
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
    @Operation(summary = "Ban user", description = "Ban a user account. Sends notification & email with optional reason.")
    public org.springframework.http.ResponseEntity<?> banUser(
            @org.springframework.web.bind.annotation.PathVariable Long userId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String reason) {
        try {
            adminService.banUser(userId);
            String finalReason = reason != null && !reason.isBlank() ? reason : "Violation of marketplace policies";
            notificationService.create(
                userId,
                "USER_BANNED",
                "Account Banned",
                "Your account has been banned. Reason: " + finalReason + "\nIf you believe this is a mistake you may appeal by replying to this email.",
                true
            );
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "User banned successfully");
            resp.put("userId", userId);
            resp.put("reason", finalReason);
            return org.springframework.http.ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to ban user: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(err);
        }
    }
    //unban user endpoint
    @org.springframework.web.bind.annotation.PostMapping("/unban-user/{userId}")
    @Operation(summary = "Unban user", description = "Restore a banned user account. Sends notification & email.")
    public org.springframework.http.ResponseEntity<?> unbanUser(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        try {
            adminService.unbanUser(userId);
            notificationService.create(
                userId,
                "USER_UNBANNED",
                "Account Restored",
                "Your account access has been restored. You may continue using the marketplace. Please ensure future compliance with policies.",
                true
            );
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "User unbanned successfully");
            resp.put("userId", userId);
            return org.springframework.http.ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to unban user: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(err);
        }
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
    // @PreAuthorize("hasRole('SUPER_ADMIN')") // Removed for easier frontend access during development
    @org.springframework.web.bind.annotation.DeleteMapping("/delete-user/{userId}")
    @Operation(summary = "Delete user permanently", description = "Super admin only - Permanently deletes a user from the system")
    public org.springframework.http.ResponseEntity<String> deleteUser(@org.springframework.web.bind.annotation.PathVariable Long userId) {
        adminService.deleteUser(userId);
        return org.springframework.http.ResponseEntity.ok("User with ID " + userId + " has been permanently deleted.");
    }

    // ==================== MODERATION ENDPOINTS ====================
    
    /**
     * Get all prohibited keywords
     */
    @org.springframework.web.bind.annotation.GetMapping("/prohibited-keywords")
    @Operation(summary = "Get all prohibited keywords", description = "Get list of all prohibited keywords for moderation")
    public org.springframework.http.ResponseEntity<List<ProhibitedKeyword>> getAllProhibitedKeywords() {
        return org.springframework.http.ResponseEntity.ok(prohibitedKeywordRepository.findAll());
    }

    /**
     * Get prohibited keywords by category
     */
    @org.springframework.web.bind.annotation.GetMapping("/prohibited-keywords/category/{category}")
    @Operation(summary = "Get keywords by category", description = "Get prohibited keywords for specific category (drugs, weapons, etc.)")
    public org.springframework.http.ResponseEntity<List<ProhibitedKeyword>> getKeywordsByCategory(
            @org.springframework.web.bind.annotation.PathVariable String category) {
        return org.springframework.http.ResponseEntity.ok(prohibitedKeywordRepository.findByCategory(category));
    }

    /**
     * Add prohibited keyword
     */
    @org.springframework.web.bind.annotation.PostMapping("/prohibited-keywords")
    @Operation(summary = "Add prohibited keyword", description = "Add new prohibited keyword to moderation system")
    public org.springframework.http.ResponseEntity<?> addProhibitedKeyword(
            @org.springframework.web.bind.annotation.RequestBody ProhibitedKeywordRequest request,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            ProhibitedKeyword keyword = new ProhibitedKeyword();
            keyword.setKeyword(request.getKeyword().toLowerCase().trim());
            keyword.setCategory(request.getCategory());
            keyword.setSeverity(request.getSeverity());
            keyword.setAutoAction(request.getAutoAction());
            keyword.setDescription(request.getDescription());
            keyword.setAddedBy(adminId);
            keyword.setAddedDate(LocalDateTime.now());
            keyword.setIsActive(true);

            ProhibitedKeyword saved = prohibitedKeywordRepository.save(keyword);
            return org.springframework.http.ResponseEntity.ok(saved);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to add keyword: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete prohibited keyword
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/prohibited-keywords/{keywordId}")
    @Operation(summary = "Delete prohibited keyword", description = "Remove keyword from moderation system")
    public org.springframework.http.ResponseEntity<?> deleteProhibitedKeyword(
            @org.springframework.web.bind.annotation.PathVariable Long keywordId) {
        try {
            prohibitedKeywordRepository.deleteById(keywordId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Keyword deleted successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete keyword: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get pending product reports (moderation queue)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reports/pending")
    @Operation(summary = "Get pending reports", description = "Get all pending product reports for moderation queue")
    public org.springframework.http.ResponseEntity<List<ProductReport>> getPendingReports() {
        return org.springframework.http.ResponseEntity.ok(reportRepository.findPendingReports());
    }

    /**
     * Review product report
     */
    @org.springframework.web.bind.annotation.PostMapping("/reports/{reportId}/review")
    @Operation(summary = "Review product report", description = "Admin reviews a product report and takes action")
    public org.springframework.http.ResponseEntity<?> reviewReport(
            @org.springframework.web.bind.annotation.PathVariable Long reportId,
            @org.springframework.web.bind.annotation.RequestParam String action, // approved, rejected, remove_product
            @org.springframework.web.bind.annotation.RequestParam(required = false) String adminNotes,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            ProductReport report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            // Normalize to a small set of statuses for consistency with frontend
            String normalizedStatus = switch (action) {
                case "approved", "approve", "flag" -> "approved";
                case "rejected", "reject" -> "rejected";
                case "remove_product", "remove" -> "approved"; // treated as approved with product removal
                default -> action;
            };

            report.setStatus(normalizedStatus);
            report.setReviewedBy(adminId);
            report.setReviewedAt(LocalDateTime.now());
            report.setAdminNotes(adminNotes);
            reportRepository.save(report);

            // If action is to approve OR remove, apply moderation to product
            if ("remove_product".equals(action) || "approved".equals(normalizedStatus)) {
                MarketPlaceProduct product = productRepository.findById(report.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                product.setFlagged(true);
                product.setFlagReason("Admin review: " + (adminNotes != null ? adminNotes : "Violates policy"));
                if ("remove_product".equals(action)) {
                    product.setStatus("removed");
                    // Notify seller about removal
                    notificationService.create(
                        product.getSellerId(),
                        "PRODUCT_REMOVED",
                        "Product Removed: " + product.getProductName(),
                        "Your product has been removed by admin. Reason: " + (adminNotes != null ? adminNotes : "Violates policy"),
                        true
                    );
                } else {
                    // approved -> hide product (soft action)
                    product.setStatus("hidden");
                    // Notify seller about hiding
                    notificationService.create(
                        product.getSellerId(),
                        "PRODUCT_HIDDEN",
                        "Product Hidden: " + product.getProductName(),
                        "Your product has been hidden by admin. Reason: " + (adminNotes != null ? adminNotes : "Violates policy"),
                        true
                    );
                }
                productRepository.save(product);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report reviewed successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to review report: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    // Lightweight DTO for enriched report row in admin UI
    private record AdminProductReport(
            Long reportId,
            Long productId,
            String productName,
            Long reporterId,
            String reporterEmail,
            String reportReason,
            String reportDetails,
            java.time.LocalDateTime reportDate,
            String status) {}

    /**
     * Enriched pending reports for admin list (includes product name and reporter email)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reports/pending/detailed")
    @Operation(summary = "Get pending reports (detailed)", description = "Pending reports with product name and reporter email for admin table")
    public org.springframework.http.ResponseEntity<java.util.List<AdminProductReport>> getPendingReportsDetailed() {
        List<ProductReport> pending = reportRepository.findPendingReports();
        java.util.List<AdminProductReport> rows = pending.stream().map(r -> {
            String productName = null;
            try {
                productName = productRepository.findById(r.getProductId()).map(MarketPlaceProduct::getProductName).orElse(null);
            } catch (Exception ignored) {}
            String reporterEmail = null;
            try {
                reporterEmail = appUserRepository.findById(r.getReporterId()).map(AppUser::getEmail).orElse(null);
            } catch (Exception ignored) {}
            return new AdminProductReport(
                    r.getReportId(),
                    r.getProductId(),
                    productName,
                    r.getReporterId(),
                    reporterEmail,
                    r.getReportReason(),
                    r.getReportDetails(),
                    r.getReportDate(),
                    r.getStatus()
            );
        }).toList();
        return org.springframework.http.ResponseEntity.ok(rows);
    }

    /**
     * Paginated & filtered reports (status + free-text query)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reports")
    @Operation(summary = "List reports (paginated)", description = "Pagination + optional status & q filter. Page numbering is 0-based.")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> listReports(
            @org.springframework.web.bind.annotation.RequestParam(name = "page", defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(name = "size", defaultValue = "20") int size,
            @org.springframework.web.bind.annotation.RequestParam(name = "status", required = false) String status,
            @org.springframework.web.bind.annotation.RequestParam(name = "q", required = false) String q) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<ProductReport> resultPage = reportRepository.searchReports(status, (q == null || q.isBlank()) ? null : q.trim(), pageable);

        java.util.List<java.util.Map<String, Object>> content = resultPage.getContent().stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("reportId", r.getReportId());
            m.put("productId", r.getProductId());
            m.put("reporterId", r.getReporterId());
            m.put("reportReason", r.getReportReason());
            m.put("reportDetails", r.getReportDetails());
            m.put("reportDate", r.getReportDate());
            m.put("status", r.getStatus());
            return m;
        }).toList();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("page", resultPage.getNumber());
        response.put("size", resultPage.getSize());
        response.put("totalElements", resultPage.getTotalElements());
        response.put("totalPages", resultPage.getTotalPages());
        response.put("content", content);
        return org.springframework.http.ResponseEntity.ok(response);
    }

    /**
     * Get single report detail (with enrichment)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reports/{reportId}")
    @Operation(summary = "Get report detail", description = "Single report with product name and reporter email")
    public org.springframework.http.ResponseEntity<?> getReportDetail(@org.springframework.web.bind.annotation.PathVariable Long reportId) {
        ProductReport r = reportRepository.findById(reportId).orElse(null);
        if (r == null) {
            java.util.Map<String, Object> err = new java.util.HashMap<>();
            err.put("success", false);
            err.put("message", "Report not found");
            return org.springframework.http.ResponseEntity.status(404).body(err);
        }
        String productName = productRepository.findById(r.getProductId()).map(MarketPlaceProduct::getProductName).orElse(null);
        String reporterEmail = appUserRepository.findById(r.getReporterId()).map(AppUser::getEmail).orElse(null);
        AdminProductReport dto = new AdminProductReport(
                r.getReportId(),
                r.getProductId(),
                productName,
                r.getReporterId(),
                reporterEmail,
                r.getReportReason(),
                r.getReportDetails(),
                r.getReportDate(),
                r.getStatus()
        );
        return org.springframework.http.ResponseEntity.ok(dto);
    }

    /**
     * Count of pending reports (for sidebar badge)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reports/pending/count")
    @Operation(summary = "Count pending reports", description = "Returns numeric count of pending product reports")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Long>> countPendingReports() {
        long count = reportRepository.findByStatus("pending").size();
        java.util.Map<String, Long> resp = new java.util.HashMap<>();
        resp.put("pending", count);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    /**
     * Aggregate stats for admin dashboard (counts + averages)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reports/stats")
    @Operation(summary = "Report stats", description = "Counts by status and average resolution/pending age in minutes")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> getReportStats() {
    long total = reportRepository.count();
    long pending = reportRepository.countByStatus("pending");
    long approved = reportRepository.countByStatus("approved");
    long rejected = reportRepository.countByStatus("rejected");

    // Average resolution time for resolved reports (approved or rejected)
    java.util.List<ProductReport> resolved = new java.util.ArrayList<>();
    resolved.addAll(reportRepository.findByStatus("approved"));
    resolved.addAll(reportRepository.findByStatus("rejected"));
    java.util.OptionalDouble avgResolutionMinutes = resolved.stream()
        .filter(r -> r.getReviewedAt() != null && r.getReportDate() != null)
        .mapToLong(r -> java.time.Duration.between(r.getReportDate(), r.getReviewedAt()).toMinutes())
        .average();

    // Pending age average
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    java.util.List<ProductReport> pendingList = reportRepository.findByStatus("pending");
    java.util.OptionalDouble avgPendingAgeMinutes = pendingList.stream()
        .filter(r -> r.getReportDate() != null)
        .mapToLong(r -> java.time.Duration.between(r.getReportDate(), now).toMinutes())
        .average();
    long oldestPendingAgeMinutes = pendingList.stream()
        .filter(r -> r.getReportDate() != null)
        .mapToLong(r -> java.time.Duration.between(r.getReportDate(), now).toMinutes())
        .max()
        .orElse(0);

    java.util.Map<String, Object> stats = new java.util.HashMap<>();
    stats.put("total", total);
    stats.put("pending", pending);
    stats.put("approved", approved);
    stats.put("rejected", rejected);
    stats.put("avgResolutionMinutes", avgResolutionMinutes.isPresent() ? avgResolutionMinutes.getAsDouble() : null);
    stats.put("avgPendingAgeMinutes", avgPendingAgeMinutes.isPresent() ? avgPendingAgeMinutes.getAsDouble() : null);
    stats.put("oldestPendingAgeMinutes", oldestPendingAgeMinutes);
    return org.springframework.http.ResponseEntity.ok(stats);
    }

    /**
     * Get all flagged products
     */
    @org.springframework.web.bind.annotation.GetMapping("/products/flagged")
    @Operation(summary = "Get flagged products", description = "Get all products flagged by moderation system")
    public org.springframework.http.ResponseEntity<List<MarketPlaceProduct>> getFlaggedProducts() {
        return org.springframework.http.ResponseEntity.ok(productRepository.findByFlagged(true));
    }

        /**
     * Unflag a product (admin override)
     */
    @org.springframework.web.bind.annotation.PostMapping("/products/{productId}/unflag")
    @Operation(summary = "Unflag product", description = "Admin removes flag from product")
    public org.springframework.http.ResponseEntity<?> unflagProduct(@org.springframework.web.bind.annotation.PathVariable Long productId) {
        try {
            MarketPlaceProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            product.setFlagged(false);
            product.setFlagReason(null);
            productRepository.save(product);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product unflagged successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to unflag product: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Hide a product (admin moderation)
     */
    @org.springframework.web.bind.annotation.PostMapping("/products/{productId}/hide")
    @Operation(summary = "Hide product", description = "Admin hides a product from public view")
    public org.springframework.http.ResponseEntity<?> hideProduct(
            @org.springframework.web.bind.annotation.PathVariable Long productId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String reason,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            MarketPlaceProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            product.setStatus("hidden");
            product.setFlagged(true);
            product.setFlagReason(reason != null ? reason : "Hidden by admin");
            productRepository.save(product);

            // Notify seller
            notificationService.create(
                product.getSellerId(),
                "PRODUCT_HIDDEN",
                "Product Hidden: " + product.getProductName(),
                "Your product has been hidden by admin. Reason: " + product.getFlagReason(),
                true
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product hidden successfully");
            response.put("productId", productId);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to hide product: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Unhide a product (admin moderation)
     */
    @org.springframework.web.bind.annotation.PostMapping("/products/{productId}/unhide")
    @Operation(summary = "Unhide product", description = "Admin restores a hidden product to public view")
    public org.springframework.http.ResponseEntity<?> unhideProduct(
            @org.springframework.web.bind.annotation.PathVariable Long productId,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            MarketPlaceProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            product.setStatus("Available");
            product.setFlagged(false);
            product.setFlagReason(null);
            productRepository.save(product);

            // Notify seller of restoration
            notificationService.create(
                product.getSellerId(),
                "PRODUCT_UNHIDDEN",
                "Product Restored: " + product.getProductName(),
                "Your product has been restored and is now visible to buyers.",
                true
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product unhidden successfully");
            response.put("productId", productId);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to unhide product: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    // ==================== REVIEW MODERATION ENDPOINTS ====================

    /**
     * Get all seller reviews (for moderation)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reviews/seller/all")
    @Operation(summary = "Get all seller reviews", description = "Admin endpoint to fetch all seller reviews for moderation")
    public org.springframework.http.ResponseEntity<?> getAllSellerReviews() {
        try {
            return org.springframework.http.ResponseEntity.ok(sellerReviewsRepository.findAll());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch reviews: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get all buyer reviews (for moderation)
     */
    @org.springframework.web.bind.annotation.GetMapping("/reviews/buyer/all")
    @Operation(summary = "Get all buyer reviews", description = "Admin endpoint to fetch all buyer reviews for moderation")
    public org.springframework.http.ResponseEntity<?> getAllBuyerReviews() {
        try {
            return org.springframework.http.ResponseEntity.ok(buyerReviewRepository.findAll());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch reviews: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Flag a seller review
     */
    @org.springframework.web.bind.annotation.PostMapping("/reviews/seller/{reviewId}/flag")
    @Operation(summary = "Flag seller review", description = "Admin flags a seller review for inappropriate content")
    public org.springframework.http.ResponseEntity<?> flagSellerReview(
            @org.springframework.web.bind.annotation.PathVariable Long reviewId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String reason,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            com.uon.marketplace.entities.SellerReviews review = sellerReviewsRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));
            
            review.setFlagged(true);
            review.setFlagReason(reason != null ? reason : "Flagged by admin");
            sellerReviewsRepository.save(review);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review flagged successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to flag review: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Flag a buyer review
     */
    @org.springframework.web.bind.annotation.PostMapping("/reviews/buyer/{reviewId}/flag")
    @Operation(summary = "Flag buyer review", description = "Admin flags a buyer review for inappropriate content")
    public org.springframework.http.ResponseEntity<?> flagBuyerReview(
            @org.springframework.web.bind.annotation.PathVariable Long reviewId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String reason,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            com.uon.marketplace.entities.BuyerReviews review = buyerReviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));
            
            review.setFlagged(true);
            review.setFlagReason(reason != null ? reason : "Flagged by admin");
            buyerReviewRepository.save(review);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review flagged successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to flag review: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Hide a seller review
     */
    @org.springframework.web.bind.annotation.PostMapping("/reviews/seller/{reviewId}/hide")
    @Operation(summary = "Hide seller review", description = "Admin hides a seller review from public view")
    public org.springframework.http.ResponseEntity<?> hideSellerReview(
            @org.springframework.web.bind.annotation.PathVariable Long reviewId,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            com.uon.marketplace.entities.SellerReviews review = sellerReviewsRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));
            
            review.setHidden(true);
            sellerReviewsRepository.save(review);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review hidden successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to hide review: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Hide a buyer review
     */
    @org.springframework.web.bind.annotation.PostMapping("/reviews/buyer/{reviewId}/hide")
    @Operation(summary = "Hide buyer review", description = "Admin hides a buyer review from public view")
    public org.springframework.http.ResponseEntity<?> hideBuyerReview(
            @org.springframework.web.bind.annotation.PathVariable Long reviewId,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            com.uon.marketplace.entities.BuyerReviews review = buyerReviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));
            
            review.setHidden(true);
            buyerReviewRepository.save(review);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review hidden successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to hide review: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete a seller review
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/reviews/seller/{reviewId}")
    @Operation(summary = "Delete seller review", description = "Admin permanently deletes a seller review")
    public org.springframework.http.ResponseEntity<?> deleteSellerReview(
            @org.springframework.web.bind.annotation.PathVariable Long reviewId,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            sellerReviewsRepository.deleteById(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review deleted successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete review: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete a buyer review
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/reviews/buyer/{reviewId}")
    @Operation(summary = "Delete buyer review", description = "Admin permanently deletes a buyer review")
    public org.springframework.http.ResponseEntity<?> deleteBuyerReview(
            @org.springframework.web.bind.annotation.PathVariable Long reviewId,
            @org.springframework.web.bind.annotation.RequestHeader("userId") Long adminId) {
        try {
            buyerReviewRepository.deleteById(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review deleted successfully");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete review: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }
}
