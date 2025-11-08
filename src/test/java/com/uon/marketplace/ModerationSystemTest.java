package com.uon.marketplace;

import com.uon.marketplace.dto.responses.ModerationResult;
import com.uon.marketplace.services.ContentModerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ModerationSystemTest {

    @Autowired
    private ContentModerationService moderationService;

    @Test
    public void testProhibitedKeywordDetection_HighSeverity() {
        // Test that "cocaine" (high severity) is detected
        String fullText = "Special herbs Selling cocaine and other stuff";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Other");
        
        assertFalse(result.isApproved(), "Product with 'cocaine' should be rejected");
        assertTrue(result.getReason().toLowerCase().contains("prohibited"), "Reason should mention prohibited");
    }

    @Test
    public void testProhibitedKeywordDetection_MediumSeverity() {
        // Test that "wire transfer" (medium severity) flags the product
        String fullText = "Laptop for sale Payment via wire transfer only";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Electronics");
        
        // Medium severity should flag but not reject
        assertTrue(result.isFlagged() || !result.isApproved(), "Product with 'wire transfer' should be flagged");
    }

    @Test
    public void testCleanProduct() {
        // Test that clean product passes moderation
        String fullText = "iPhone 13 Pro Excellent condition, barely used";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Electronics");
        
        assertTrue(result.isApproved(), "Clean product should be approved");
        assertFalse(result.isFlagged(), "Clean product should not be flagged");
    }

    @Test
    public void testProfanityDetection() {
        // Test profanity filter
        String fullText = "Laptop This shit is amazing, fuck yeah!";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Electronics");
        
        assertTrue(result.isFlagged() || !result.isApproved(), "Product with profanity should be flagged/rejected");
    }

    @Test
    public void testMultipleProhibitedKeywords() {
        // Test multiple keywords
        String fullText = "Special items Selling marijuana and alcohol, wire transfer only";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Other");
        
        assertFalse(result.isApproved(), "Product with multiple prohibited keywords should be rejected");
    }

    @Test
    public void testCaseInsensitiveDetection() {
        // Test that detection is case-insensitive
        String fullText = "Items COCAINE for sale";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Other");
        
        assertFalse(result.isApproved(), "Uppercase 'COCAINE' should still be detected");
    }

    @Test
    public void testKeywordInTitle() {
        // Test that keywords in title are also detected
        String fullText = "Cocaine for sale Good quality";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Other");
        
        assertFalse(result.isApproved(), "Prohibited keyword in title should be detected");
    }

    @Test
    public void testScamIndicators() {
        // Test scam-related keywords
        String fullText = "iPhone 13 Send money via PayPal immediately, quick cash deal";
        
        ModerationResult result = moderationService.moderateProductText(fullText, "Electronics");
        
        assertTrue(result.isFlagged() || !result.isApproved(), "Scam indicators should flag product");
    }
}
