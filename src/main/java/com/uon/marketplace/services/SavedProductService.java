package com.uon.marketplace.services;

import com.uon.marketplace.entities.SavedProduct;
import com.uon.marketplace.repositories.SavedProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavedProductService {
    @Autowired
    private SavedProductRepository savedProductRepository;

    public SavedProduct saveProduct(Long userId, Long productId) {
        if (savedProductRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Product already saved");
        }
        
        SavedProduct savedProduct = new SavedProduct();
        savedProduct.setUserId(userId);
        savedProduct.setProductId(productId);
        savedProduct.setSavedDate(LocalDateTime.now());
        
        return savedProductRepository.save(savedProduct);
    }

    @Transactional
    public void unsaveProduct(Long userId, Long productId) {
        savedProductRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public List<SavedProduct> getSavedProductsByUser(Long userId) {
        return savedProductRepository.findByUserId(userId);
    }

    public boolean isProductSaved(Long userId, Long productId) {
        return savedProductRepository.existsByUserIdAndProductId(userId, productId);
    }
}
