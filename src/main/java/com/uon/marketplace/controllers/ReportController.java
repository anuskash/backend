package com.uon.marketplace.controllers;

import com.uon.marketplace.dto.requests.ProductReportRequest;
import com.uon.marketplace.entities.ProductReport;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.repositories.ProductReportRepository;
import com.uon.marketplace.repositories.MarketPlaceProductRepository;
import com.uon.marketplace.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ProductReportRepository reportRepository;

    @Autowired
    private MarketPlaceProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Submit a product report
     */
    @PostMapping("/product")
    public ResponseEntity<?> reportProduct(@RequestBody ProductReportRequest request,
                                          @RequestHeader("userId") Long reporterId) {
        try {
            // Validate product exists
            MarketPlaceProduct product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Check if user already reported this product
            if (reportRepository.existsByProductIdAndReporterId(request.getProductId(), reporterId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You have already reported this product");
                return ResponseEntity.badRequest().body(response);
            }

            // Create report
            ProductReport report = new ProductReport();
            report.setProductId(request.getProductId());
            report.setReporterId(reporterId);
            report.setReportReason(request.getReportReason());
            report.setReportDetails(request.getReportDetails());
            report.setReportDate(LocalDateTime.now());
            report.setStatus("pending");

            reportRepository.save(report);

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
            
            productRepository.save(product);

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

    /**
     * Get user's report history
     */
    @GetMapping("/my-reports")
    public ResponseEntity<?> getMyReports(@RequestHeader("userId") Long userId) {
        try {
            return ResponseEntity.ok(reportRepository.findByReporterId(userId));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch reports: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
