package com.uon.marketplace.services;

import com.uon.marketplace.dto.requests.MarketPlaceProductRequest;
import com.uon.marketplace.dto.responses.ModerationResult;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.ProductImage;
import com.uon.marketplace.repositories.MarketPlaceProductRepository;
import com.uon.marketplace.repositories.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MarketPlaceProductService {
    @Autowired
    private MarketPlaceProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private ContentModerationService moderationService;

    @Autowired
    private NotificationService notificationService;

    public List<MarketPlaceProduct> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<MarketPlaceProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public MarketPlaceProduct createProduct(MarketPlaceProduct request) {
        // Moderate the product before creating
        ModerationResult moderationResult = moderationService.moderateProduct(request);
        
        MarketPlaceProduct product = new MarketPlaceProduct();
        product.setSellerId(request.getSellerId());
        product.setProductName(request.getProductName());
        product.setProductDescription(request.getProductDescription());
        product.setSellerName(request.getSellerName());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setPrice(request.getPrice());
        product.setSellerName(request.getSellerName());
        product.setPostedDate(LocalDateTime.now());
        product.setLastUpdate(LocalDateTime.now());
        
        // Handle moderation results
        if (moderationResult.isRejected()) {
            // Notify seller about rejection before throwing
            try {
                notificationService.create(
                        request.getSellerId(),
                        "PRODUCT_REJECTED",
                        "Product Rejected: " + request.getProductName(),
                        "Your product was rejected during submission. Reason: " + moderationResult.getReason(),
                        true
                );
            } catch (Exception ignored) {}
            throw new RuntimeException("Product rejected: " + moderationResult.getReason());
        } else if (moderationResult.isFlagged()) {
            // Flag for review but allow creation
            product.setStatus("pending_review");
            product.setFlagged(true);
            product.setFlagReason(moderationResult.getReason());
            try {
                notificationService.create(
                        request.getSellerId(),
                        "PRODUCT_FLAGGED",
                        "Product Flagged for Review: " + request.getProductName(),
                        "Your product is pending review. Reason: " + moderationResult.getReason(),
                        true
                );
            } catch (Exception ignored) {}
        } else {
            // Approved
            product.setStatus(request.getStatus());
            product.setFlagged(false);
        }
        
        // Set productImageUrl if provided (for backward compatibility)
        if (request.getProductImageUrl() != null && !request.getProductImageUrl().isEmpty()) {
            product.setProductImageUrl(request.getProductImageUrl());
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    public void saveProductImages(Long productId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        
        // Delete existing images
        productImageRepository.deleteByProductId(productId);
        
        // Save new images
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setDisplayOrder(i);
            image.setIsPrimary(i == 0); // First image is primary
            productImageRepository.save(image);
        }
        
        // Update the main product table with the first image for backward compatibility
        if (!imageUrls.isEmpty()) {
            productRepository.findById(productId).ifPresent(product -> {
                product.setProductImageUrl(imageUrls.get(0));
                productRepository.save(product);
            });
        }
    }
    
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }

    public void deleteImageByUrl(String imageUrl) {
        productImageRepository.deleteByImageUrl(imageUrl);
    }

   public MarketPlaceProduct updateProductPrice(Long productId, java.math.BigDecimal newPrice) {
        return productRepository.findById(productId)
                .map(product -> {
                    product.setPrice(newPrice);
                    product.setLastUpdate(LocalDateTime.now());
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    public MarketPlaceProduct updateProductStatus(Long productId, String newStatus) {
        return productRepository.findById(productId)
                .map(product -> {
                    product.setStatus(newStatus);
                    product.setLastUpdate(LocalDateTime.now());
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }


    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public MarketPlaceProduct markProductSold(Long productId, Long buyerId, String buyerName) {
        return productRepository.findById(productId)
                .map(product -> {
                    product.setBuyerId(buyerId);
                    product.setBuyerName(buyerName);
                    product.setStatus("Sold");
                    product.setLastUpdate(LocalDateTime.now());
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    public List<MarketPlaceProduct> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public List<MarketPlaceProduct> getProductsByBuyerId(Long buyerId) {
        return productRepository.findByBuyerId(buyerId);
    }

    public List<MarketPlaceProduct> getAvailableProducts() {
        return productRepository.findByStatus("Available");
    }

    @Transactional
    public MarketPlaceProduct updateProduct(Long productId, com.uon.marketplace.dto.requests.UpdateProductRequest request) {
        MarketPlaceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Create a temp product for moderation check
        MarketPlaceProduct tempProduct = new MarketPlaceProduct();
        tempProduct.setProductName(request.getProductName() != null ? request.getProductName() : product.getProductName());
        tempProduct.setProductDescription(request.getProductDescription() != null ? request.getProductDescription() : product.getProductDescription());
        
        // Moderate the updated content
        ModerationResult moderationResult = moderationService.moderateProduct(tempProduct);
        
        if (moderationResult.isRejected()) {
            // Notify seller about rejection of update
            try {
                notificationService.create(
                        product.getSellerId(),
                        "PRODUCT_UPDATE_REJECTED",
                        "Update Rejected: " + product.getProductName(),
                        "Your product update was rejected. Reason: " + moderationResult.getReason(),
                        true
                );
            } catch (Exception ignored) {}
            throw new RuntimeException("Product update rejected: " + moderationResult.getReason());
        }
        
        // Update fields if provided
        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getProductDescription() != null) {
            product.setProductDescription(request.getProductDescription());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getCondition() != null) {
            product.setCondition(request.getCondition());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        
        // Handle moderation flags
        if (moderationResult.isFlagged()) {
            boolean newlyFlagged = Boolean.FALSE.equals(product.getFlagged());
            product.setFlagged(true);
            product.setFlagReason(moderationResult.getReason());
            product.setStatus("pending_review");
            if (newlyFlagged) {
                try {
                    notificationService.create(
                            product.getSellerId(),
                            "PRODUCT_FLAGGED",
                            "Product Flagged for Review: " + product.getProductName(),
                            "Your product was flagged during update. Reason: " + moderationResult.getReason(),
                            true
                    );
                } catch (Exception ignored) {}
            }
        }
        
        product.setLastUpdate(LocalDateTime.now());
        MarketPlaceProduct savedProduct = productRepository.save(product);
        
        // Update images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            saveProductImages(productId, request.getImageUrls());
        }
        
        return savedProduct;
    }

    // Direct save method for internal use (e.g., updating report count, flags)
    public MarketPlaceProduct saveProductDirectly(MarketPlaceProduct product) {
        return productRepository.save(product);
    }
    // check moderation result for product request
    public ModerationResult checkModerationResult(MarketPlaceProductRequest product) {
        return moderationService.checkModerationResult(product);
    }
}