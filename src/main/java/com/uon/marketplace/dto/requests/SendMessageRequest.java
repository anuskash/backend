package com.uon.marketplace.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private Long receiverId;
    private Long productId;
    private String content;
}
