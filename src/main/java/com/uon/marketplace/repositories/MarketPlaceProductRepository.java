package com.uon.marketplace.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uon.marketplace.entities.MarketPlaceProduct;

@Repository
public interface MarketPlaceProductRepository extends JpaRepository<MarketPlaceProduct, Long> {

    List<MarketPlaceProduct> findBySellerId(Long sellerId);

    List<MarketPlaceProduct> findByBuyerId(Long buyerId);

    List<MarketPlaceProduct> findByStatus(String status);
    
    List<MarketPlaceProduct> findBySellerIdAndStatusIn(Long sellerId, List<String> statuses);
    
    List<MarketPlaceProduct> findByBuyerIdAndStatusIn(Long buyerId, List<String> statuses);
    
    List<MarketPlaceProduct> findBySellerIdAndStatus(Long sellerId, String status);
    
    List<MarketPlaceProduct> findByBuyerIdAndStatus(Long buyerId, String status);
    
    // Moderation-related queries
    List<MarketPlaceProduct> findByFlagged(Boolean flagged);
}

