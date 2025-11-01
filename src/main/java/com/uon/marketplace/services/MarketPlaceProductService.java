package com.uon.marketplace.services;

import com.uon.marketplace.dto.requests.MarketPlaceProductRequest;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.repositories.MarketPlaceProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MarketPlaceProductService {
    @Autowired
    private MarketPlaceProductRepository productRepository;

    public List<MarketPlaceProduct> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<MarketPlaceProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public MarketPlaceProduct createProduct(MarketPlaceProduct request) {
        MarketPlaceProduct product = new MarketPlaceProduct();
        product.setSellerId(request.getSellerId());
        product.setProductName(request.getProductName());
        product.setProductDescription(request.getProductDescription());
        product.setProductImageUrl(request.getProductImageUrl());
        product.setSellerName(request.getSellerName());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());
        product.setSellerName(request.getSellerName());
        product.setPostedDate(LocalDateTime.now());
        product.setLastUpdate(LocalDateTime.now());
        return productRepository.save(product);
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