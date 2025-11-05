package com.uon.marketplace.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uon.marketplace.dto.requests.SendMessageRequest;
import com.uon.marketplace.dto.responses.ConversationResponse;
import com.uon.marketplace.dto.responses.MessageResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.MarketPlaceProduct;
import com.uon.marketplace.entities.Message;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.repositories.AppUserRepository;
import com.uon.marketplace.repositories.MarketPlaceProductRepository;
import com.uon.marketplace.repositories.MessageRepository;
import com.uon.marketplace.repositories.UserProfileRepository;

@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private AppUserRepository appUserRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private MarketPlaceProductRepository productRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Send a new message and notify receiver via email
     */
    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        // Validate sender exists
        AppUser sender = appUserRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        // Validate receiver exists
        AppUser receiver = appUserRepository.findById(request.getReceiverId())
            .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        // Validate product exists
        MarketPlaceProduct product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Create and save message
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setProductId(request.getProductId());
        message.setContent(request.getContent());
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);
        
        message = messageRepository.save(message);
        
        // Send email notification to receiver
        sendMessageNotification(sender, receiver, product, message);
        
        return convertToMessageResponse(message);
    }
    
    /**
     * Get all conversations for a user (inbox view)
     */
    public List<ConversationResponse> getConversations(Long userId) {
        List<Message> latestMessages = messageRepository.findLatestConversations(userId);
        
        // Group messages by (otherUser, product) combination
        Map<String, ConversationResponse> conversationMap = new HashMap<>();
        
        for (Message msg : latestMessages) {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            String key = otherUserId + "_" + msg.getProductId();
            
            if (!conversationMap.containsKey(key)) {
                ConversationResponse conv = buildConversationResponse(userId, msg, otherUserId);
                conversationMap.put(key, conv);
            }
        }
        
        return new ArrayList<>(conversationMap.values());
    }
    
    /**
     * Get all messages in a specific conversation
     */
    public List<MessageResponse> getConversationMessages(Long userId, Long otherUserId, Long productId) {
        List<Message> messages = messageRepository.findConversation(userId, otherUserId, productId);
        
        // Mark messages as read if current user is the receiver
        messages.forEach(msg -> {
            if (msg.getReceiverId().equals(userId) && !msg.getIsRead()) {
                msg.markAsRead();
                messageRepository.save(msg);
            }
        });
        
        return messages.stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get count of unread messages for a user
     */
    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }
    
    /**
     * Mark a specific message as read
     */
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Only receiver can mark as read
        if (!message.getReceiverId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only receiver can mark message as read");
        }
        
        if (!message.getIsRead()) {
            message.markAsRead();
            messageRepository.save(message);
        }
    }
    
    /**
     * Send email notification when a new message is received
     */
    private void sendMessageNotification(AppUser sender, AppUser receiver, 
                                        MarketPlaceProduct product, Message message) {
        try {
            UserProfile senderProfile = userProfileRepository.findByUserId(sender.getUserId())
                .orElse(null);
            
            String senderName = senderProfile != null 
                ? senderProfile.getFirstName() + " " + senderProfile.getLastName()
                : sender.getEmail();
            
            String subject = "New message about \"" + product.getProductName() + "\"";
            String body = String.format(
                "Hi,\n\n" +
                "You have received a new message from %s regarding the product \"%s\".\n\n" +
                "Message:\n%s\n\n" +
                "Log in to your UON Marketplace account to view and reply to this message.\n\n" +
                "— UON Marketplace",
                senderName,
                product.getProductName(),
                message.getContent()
            );
            
            emailService.send(receiver.getEmail(), subject, body);
        } catch (Exception e) {
            // Log error but don't fail the message send operation
            System.err.println("Failed to send message notification email: " + e.getMessage());
        }
    }
    
    /**
     * Convert Message entity to MessageResponse DTO
     */
    private MessageResponse convertToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setMessageId(message.getMessageId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setProductId(message.getProductId());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setIsRead(message.getIsRead());
        response.setReadAt(message.getReadAt());
        
        // Fetch sender details
        appUserRepository.findById(message.getSenderId()).ifPresent(sender -> {
            response.setSenderEmail(sender.getEmail());
            userProfileRepository.findByUserId(sender.getUserId()).ifPresent(profile -> {
                response.setSenderName(profile.getFirstName() + " " + profile.getLastName());
            });
        });
        
        // Fetch receiver details
        appUserRepository.findById(message.getReceiverId()).ifPresent(receiver -> {
            response.setReceiverEmail(receiver.getEmail());
            userProfileRepository.findByUserId(receiver.getUserId()).ifPresent(profile -> {
                response.setReceiverName(profile.getFirstName() + " " + profile.getLastName());
            });
        });
        
        // Fetch product details
        productRepository.findById(message.getProductId()).ifPresent(product -> {
            response.setProductTitle(product.getProductName());
        });
        
        return response;
    }
    
    /**
     * Build ConversationResponse from message and other user details
     */
    private ConversationResponse buildConversationResponse(Long currentUserId, Message msg, Long otherUserId) {
        ConversationResponse conv = new ConversationResponse();
        conv.setOtherUserId(otherUserId);
        conv.setProductId(msg.getProductId());
        conv.setLastMessage(msg.getContent());
        conv.setLastMessageTime(msg.getSentAt());
        
        // Get other user details
        appUserRepository.findById(otherUserId).ifPresent(user -> {
            conv.setOtherUserEmail(user.getEmail());
            userProfileRepository.findByUserId(user.getUserId()).ifPresent(profile -> {
                conv.setOtherUserName(profile.getFirstName() + " " + profile.getLastName());
            });
        });
        
        // Get product details
        productRepository.findById(msg.getProductId()).ifPresent(product -> {
            conv.setProductTitle(product.getProductName());
        });
        
        // Check for unread messages in this conversation
        List<Message> conversationMessages = messageRepository.findConversation(
            currentUserId, otherUserId, msg.getProductId());
        
        long unreadCount = conversationMessages.stream()
            .filter(m -> m.getReceiverId().equals(currentUserId) && !m.getIsRead())
            .count();
        
        conv.setUnreadCount(unreadCount);
        conv.setHasUnread(unreadCount > 0);
        
        return conv;
    }
}
