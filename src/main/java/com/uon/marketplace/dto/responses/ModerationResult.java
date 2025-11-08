package com.uon.marketplace.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationResult {
    
    private boolean approved;
    private boolean flagged;
    private boolean rejected;
    
    private String reason;
    private String severity; // 'high', 'medium', 'low'
    private List<String> detectedKeywords = new ArrayList<>();
    private String recommendedAction; // 'approve', 'flag_for_review', 'auto_reject'
    
    // Setter that returns this for chaining
    public ModerationResult setDetectedKeywords(List<String> keywords) {
        this.detectedKeywords = keywords;
        return this;
    }
    
    // Factory methods for easy creation
    public static ModerationResult approved() {
        ModerationResult result = new ModerationResult();
        result.setApproved(true);
        result.setFlagged(false);
        result.setRejected(false);
        result.setRecommendedAction("approve");
        return result;
    }
    
    public static ModerationResult flagged(String reason) {
        ModerationResult result = new ModerationResult();
        result.setApproved(false);
        result.setFlagged(true);
        result.setRejected(false);
        result.setReason(reason);
        result.setSeverity("medium");
        result.setRecommendedAction("flag_for_review");
        return result;
    }
    
    public static ModerationResult rejected(String reason) {
        ModerationResult result = new ModerationResult();
        result.setApproved(false);
        result.setFlagged(false);
        result.setRejected(true);
        result.setReason(reason);
        result.setSeverity("high");
        result.setRecommendedAction("auto_reject");
        return result;
    }
    
    public static ModerationResult flaggedWithKeywords(String reason, List<String> keywords) {
        ModerationResult result = flagged(reason);
        result.setDetectedKeywords(keywords);
        return result;
    }
}
