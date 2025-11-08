package com.uon.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId; // User who reported
    
    @Column(name = "report_reason", nullable = false, length = 50)
    private String reportReason; // 'scam', 'inappropriate', 'prohibited', 'spam', 'fake'
    
    @Column(name = "report_details", length = 500)
    private String reportDetails;
    
    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;
    
    @Column(name = "status", length = 20)
    private String status = "pending"; // 'pending', 'reviewed', 'actioned', 'dismissed'
    
    @Column(name = "reviewed_by")
    private Long reviewedBy; // Admin user_id
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "admin_notes", length = 500)
    private String adminNotes;
}
