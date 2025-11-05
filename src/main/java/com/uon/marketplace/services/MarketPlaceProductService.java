package com.uon.marketplace.services;

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

    public List<MarketPlaceProduct> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<MarketPlaceProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public MarketPlaceProduct createProduct(MarketPlaceProduct request) {
        MarketPlaceProduct product = new MarketPlaceProduct();
        product.setSellerId(request.getSellerId());
        product.setProductName(request.getProductName());
        product.setProductDescription(request.getProductDescription());
        product.setSellerName(request.getSellerName());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());
        product.setSellerName(request.getSellerName());
        product.setPostedDate(LocalDateTime.now());
        product.setLastUpdate(LocalDateTime.now());
        
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
}