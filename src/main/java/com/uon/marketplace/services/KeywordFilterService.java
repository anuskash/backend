package com.uon.marketplace.services;

import com.uon.marketplace.repositories.ProhibitedKeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class KeywordFilterService {
    
    @Autowired
    private ProhibitedKeywordRepository prohibitedKeywordRepository;
    
    // Cache for performance (refresh periodically in production)
    private List<String> cachedKeywords = new ArrayList<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 60000; // 1 minute
    
    /**
     * Check if text contains any prohibited keywords
     */
    public boolean containsProhibitedKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        List<String> keywords = getActiveKeywords();
        String lowerText = text.toLowerCase();
        
        return keywords.stream()
            .anyMatch(keyword -> containsKeywordVariation(lowerText, keyword.toLowerCase()));
    }
    
    /**
     * Check specific category (e.g., only check for drugs)
     */
    public boolean containsProhibitedKeywordsByCategory(String text, String category) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        List<String> keywords = prohibitedKeywordRepository.findKeywordsByCategory(category);
        String lowerText = text.toLowerCase();
        
        return keywords.stream()
            .anyMatch(keyword -> containsKeywordVariation(lowerText, keyword.toLowerCase()));
    }
    
    /**
     * Find all prohibited keywords in text (for detailed reporting)
     */
    public List<String> findProhibitedKeywords(String text) {
        List<String> found = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return found;
        }
        
        List<String> keywords = getActiveKeywords();
        String lowerText = text.toLowerCase();
        
        for (String keyword : keywords) {
            if (containsKeywordVariation(lowerText, keyword.toLowerCase())) {
                found.add(keyword);
            }
        }
        
        return found;
    }
    
    /**
     * Check with variations (handles common bypasses like "w33d", "m@rijuana")
     */
    private boolean containsKeywordVariation(String text, String keyword) {
        // Direct match
        if (text.contains(keyword)) {
            return true;
        }
        
        // Common character substitutions (leet speak)
        String pattern = keyword
            .replace("a", "[a@]")
            .replace("e", "[e3]")
            .replace("i", "[i1!]")
            .replace("o", "[o0]")
            .replace("s", "[s$5]");
        
        Pattern regex = Pattern.compile("\\b" + pattern + "\\b", Pattern.CASE_INSENSITIVE);
        return regex.matcher(text).find();
    }
    
    /**
     * Get active keywords with caching
     */
    private List<String> getActiveKeywords() {
        long now = System.currentTimeMillis();
        
        // Refresh cache if expired
        if (cachedKeywords.isEmpty() || (now - lastCacheUpdate) > CACHE_TTL) {
            cachedKeywords = prohibitedKeywordRepository.findAllActiveKeywords();
            lastCacheUpdate = now;
        }
        
        return cachedKeywords;
    }
    
    /**
     * Manually refresh cache (call after admin adds/removes keywords)
     */
    public void refreshCache() {
        cachedKeywords = prohibitedKeywordRepository.findAllActiveKeywords();
        lastCacheUpdate = System.currentTimeMillis();
    }
}
