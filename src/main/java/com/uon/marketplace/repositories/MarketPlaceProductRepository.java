package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.MarketPlaceProduct;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketPlaceProductRepository extends JpaRepository<MarketPlaceProduct, Long> {

    List<MarketPlaceProduct> findBySellerId(Long sellerId);
    // Custom query methods can be added here

    List<MarketPlaceProduct> findByBuyerId(Long buyerId);

    List<MarketPlaceProduct> findByStatus(String string);
}
