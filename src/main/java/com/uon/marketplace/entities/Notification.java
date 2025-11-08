package com.uon.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", length = 40, nullable = false)
    private String type; // PRODUCT_FLAGGED, PRODUCT_HIDDEN, PRODUCT_REMOVED, PRODUCT_UNHIDDEN

    @Column(name = "title", length = 120, nullable = false)
    private String title;

    @Column(name = "body", length = 500)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 'read' is a reserved keyword in SQL Server; map to a safe column name
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
