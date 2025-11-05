package com.uon.marketplace.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "content", nullable = false, length = 2000)
    private String content;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    // Helper method to mark message as read
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
