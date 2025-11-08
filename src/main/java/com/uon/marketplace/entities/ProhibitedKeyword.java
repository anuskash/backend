package com.uon.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "prohibited_keywords")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProhibitedKeyword {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long keywordId;
    
    @Column(name = "keyword", nullable = false, unique = true, length = 100)
    private String keyword;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category; // 'drugs', 'weapons', 'profanity', 'prohibited_items', 'scam_indicators'
    
    @Column(name = "severity", nullable = false, length = 20)
    private String severity; // 'high', 'medium', 'low'
    
    @Column(name = "auto_action", length = 50)
    private String autoAction; // 'reject', 'flag', 'warn'
    
    @Column(name = "added_by")
    private Long addedBy; // Admin user_id who added this keyword
    
    @Column(name = "added_date", nullable = false)
    private LocalDateTime addedDate;
    
    @Column(name = "is_active")
    private Boolean isActive = true; // Allow soft disable without deleting
    
    @Column(name = "description", length = 255)
    private String description; // Why this keyword is prohibited
}
