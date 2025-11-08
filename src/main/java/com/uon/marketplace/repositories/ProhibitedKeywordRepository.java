package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.ProhibitedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProhibitedKeywordRepository extends JpaRepository<ProhibitedKeyword, Long> {
    
    // Find all active keywords
    List<ProhibitedKeyword> findByIsActiveTrue();
    
    // Find by category
    List<ProhibitedKeyword> findByCategory(String category);
    List<ProhibitedKeyword> findByCategoryAndIsActiveTrue(String category);
    
    // Find by severity
    List<ProhibitedKeyword> findBySeverityAndIsActiveTrue(String severity);
    
    // Check if keyword exists
    boolean existsByKeywordIgnoreCase(String keyword);
    
    // Get all active keywords as strings (for fast filtering)
    @Query("SELECT pk.keyword FROM ProhibitedKeyword pk WHERE pk.isActive = true")
    List<String> findAllActiveKeywords();
    
    // Get keywords by category as strings
    @Query("SELECT pk.keyword FROM ProhibitedKeyword pk WHERE pk.category = :category AND pk.isActive = true")
    List<String> findKeywordsByCategory(@Param("category") String category);
}
