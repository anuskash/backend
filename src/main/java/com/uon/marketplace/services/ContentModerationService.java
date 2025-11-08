package com.uon.marketplace.services;

import com.uon.marketplace.dto.responses.ModerationResult;
import com.uon.marketplace.entities.MarketPlaceProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentModerationService {
    
    @Autowired
    private KeywordFilterService keywordFilter;
    
    @Autowired
    private ProfanityFilter profanityFilter;
    
    /**
     * Moderate a product listing (title + description)
     */
    public ModerationResult moderateProduct(MarketPlaceProduct product) {
        String fullText = product.getProductName() + " " + 
                         (product.getProductDescription() != null ? product.getProductDescription() : "");
        
        return moderateProductText(fullText, product.getCategory());
    }
    
    /**
     * Moderate product text (can be called before product is created)
     */
    public ModerationResult moderateProductText(String text, String category) {
        // Layer 1: Check for prohibited items (drugs, weapons, etc.)
        List<String> prohibitedItems = keywordFilter.findProhibitedKeywords(text);
        if (!prohibitedItems.isEmpty()) {
            return ModerationResult.rejected("Contains prohibited items: " + String.join(", ", prohibitedItems))
                .setDetectedKeywords(prohibitedItems);
        }
        
        // Layer 2: Check for profanity
        if (profanityFilter.containsProfanity(text)) {
            List<String> badWords = profanityFilter.findProfanity(text);
            return ModerationResult.flagged("Contains inappropriate language")
                .setDetectedKeywords(badWords);
        }
        
        // Layer 3: Check banned categories
        if (isBannedCategory(category)) {
            return ModerationResult.rejected("Category not allowed: " + category);
        }
        
        // All checks passed
        return ModerationResult.approved();
    }
    
    /**
     * Moderate a review text
     */
    public ModerationResult moderateReview(String reviewText) {
        // For reviews, we're stricter on profanity
        if (profanityFilter.containsProfanity(reviewText)) {
            List<String> badWords = profanityFilter.findProfanity(reviewText);
            return ModerationResult.rejected("Review contains profanity")
                .setDetectedKeywords(badWords);
        }
        
        // Check for scam indicators or spam
        if (keywordFilter.containsProhibitedKeywordsByCategory(reviewText, "scam_indicators")) {
            return ModerationResult.flagged("Review may contain spam or scam language");
        }
        
        return ModerationResult.approved();
    }
    
    /**
     * Moderate a message
     */
    public ModerationResult moderateMessage(String messageContent) {
        // Check profanity
        if (profanityFilter.containsProfanity(messageContent)) {
            return ModerationResult.flagged("Message contains inappropriate language");
        }
        
        // Check for prohibited items (in case users try to trade prohibited items via messages)
        if (keywordFilter.containsProhibitedKeywordsByCategory(messageContent, "drugs") ||
            keywordFilter.containsProhibitedKeywordsByCategory(messageContent, "weapons")) {
            return ModerationResult.flagged("Message may contain prohibited item references");
        }
        
        return ModerationResult.approved();
    }
    
    /**
     * Clean text by removing profanity (optional - for auto-cleaning)
     */
    public String cleanText(String text) {
        return profanityFilter.cleanText(text);
    }
    
    /**
     * Check if category is banned
     */
    private boolean isBannedCategory(String category) {
        if (category == null) return false;
        
        List<String> bannedCategories = List.of(
            "Drugs",
            "Weapons",
            "Alcohol",
            "Tobacco",
            "Vaping",
            "Adult Content"
        );
        
        return bannedCategories.stream()
            .anyMatch(banned -> banned.equalsIgnoreCase(category));
    }
}
