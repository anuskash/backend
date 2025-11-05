package com.uon.marketplace.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uon.marketplace.dto.requests.SendMessageRequest;
import com.uon.marketplace.dto.responses.ConversationResponse;
import com.uon.marketplace.dto.responses.MessageResponse;
import com.uon.marketplace.services.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/messages")
@Tag(name = "Messages", description = "In-app messaging between buyers and sellers")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @PostMapping("/send")
    @Operation(summary = "Send a message", description = "Send a message about a product to another user. Receiver gets email notification.")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestParam Long senderId,
            @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(senderId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/conversations")
    @Operation(summary = "Get all conversations", description = "Get inbox view with all unique conversations for the logged-in user")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @RequestParam Long userId) {
        List<ConversationResponse> conversations = messageService.getConversations(userId);
        return ResponseEntity.ok(conversations);
    }
    
    @GetMapping("/conversation")
    @Operation(summary = "Get conversation messages", description = "Get full chat history for a specific product and user. Automatically marks messages as read.")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            @RequestParam Long userId,
            @RequestParam Long otherUserId,
            @RequestParam Long productId) {
        List<MessageResponse> messages = messageService.getConversationMessages(userId, otherUserId, productId);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count", description = "Get the total number of unread messages for a user")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long userId) {
        Long count = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{messageId}/mark-read")
    @Operation(summary = "Mark message as read", description = "Manually mark a specific message as read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok("Message marked as read");
    }
}
