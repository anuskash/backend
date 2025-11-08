package com.uon.marketplace.services;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProfanityFilter {
    
    // Common profanity list (add your own words here)
    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(
        // Basic profanity
        "damn", "hell", "crap", "shit", "fuck", "bitch", "ass", "asshole",
        "bastard", "piss", "dick", "cock", "pussy", "whore", "slut",
        
        // Variations with numbers/symbols (will be detected by pattern matching)
        "f*ck", "sh!t", "b!tch", "a$$", "f***"
    ));
    
    // Patterns to catch variations (e.g., "f***", "sh!t", "b1tch")
    private static final List<Pattern> PROFANITY_PATTERNS = Arrays.asList(
        Pattern.compile("\\bf+[u*@#$]+c+k+\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bs+[h!#$]+[i!1]+t+\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bb+[i!1]+t+c+h+\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\ba+[s$5]+[s$5]+\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bd+[a@]+m+n+\\b", Pattern.CASE_INSENSITIVE)
    );
    
    /**
     * Check if text contains profanity
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        
        // Check exact word matches
        String[] words = lowerText.split("\\s+");
        for (String word : words) {
            // Remove punctuation
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
            if (BAD_WORDS.contains(cleanWord)) {
                return true;
            }
        }
        
        // Check patterns (catches variations like "f***", "sh!t")
        for (Pattern pattern : PROFANITY_PATTERNS) {
            if (pattern.matcher(lowerText).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Find all profane words in text
     */
    public List<String> findProfanity(String text) {
        List<String> found = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return found;
        }
        
        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("\\s+");
        
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
            if (BAD_WORDS.contains(cleanWord)) {
                found.add(word);
            }
        }
        
        return found;
    }
    
    /**
     * Clean text by replacing profanity with asterisks
     */
    public String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        String cleaned = text;
        
        // Replace exact matches
        for (String badWord : BAD_WORDS) {
            String replacement = "*".repeat(badWord.length());
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(badWord) + "\\b", Pattern.CASE_INSENSITIVE);
            cleaned = pattern.matcher(cleaned).replaceAll(replacement);
        }
        
        // Replace pattern matches
        for (Pattern pattern : PROFANITY_PATTERNS) {
            cleaned = pattern.matcher(cleaned).replaceAll(m -> "*".repeat(m.group().length()));
        }
        
        return cleaned;
    }
    
    /**
     * Add custom bad word (for admin management)
     */
    public void addBadWord(String word) {
        BAD_WORDS.add(word.toLowerCase());
    }
    
    /**
     * Remove word from filter
     */
    public void removeBadWord(String word) {
        BAD_WORDS.remove(word.toLowerCase());
    }
}
