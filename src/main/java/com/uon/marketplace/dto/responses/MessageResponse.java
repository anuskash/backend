package com.uon.marketplace.dto.responses;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;
    private Long productId;
    private String productTitle;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private LocalDateTime readAt;
}
