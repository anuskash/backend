package com.uon.marketplace.dto.responses;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private Long otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private Long productId;
    private String productTitle;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Boolean hasUnread;
    private Long unreadCount;
}
