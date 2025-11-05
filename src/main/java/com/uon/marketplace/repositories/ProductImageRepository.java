package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);
    void deleteByProductId(Long productId);
    ProductImage findByProductIdAndIsPrimaryTrue(Long productId);
    void deleteByImageUrl(String imageUrl);
}
