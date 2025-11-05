package com.uon.marketplace.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uon.marketplace.entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find all messages between two users for a specific product
     * Ordered by sent_at ascending (oldest first - natural chat order)
     */
    @Query("SELECT m FROM Message m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) AND " +
           "m.productId = :productId " +
           "ORDER BY m.sentAt ASC")
    List<Message> findConversation(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2, 
        @Param("productId") Long productId
    );
    
    /**
     * Find all unique conversations for a user
     * Groups by product and other participant, returns latest message per conversation
     */
    @Query("SELECT m FROM Message m WHERE m.messageId IN (" +
           "SELECT MAX(m2.messageId) FROM Message m2 WHERE " +
           "m2.senderId = :userId OR m2.receiverId = :userId " +
           "GROUP BY " +
           "CASE WHEN m2.senderId = :userId THEN m2.receiverId ELSE m2.senderId END, " +
           "m2.productId) " +
           "ORDER BY m.sentAt DESC")
    List<Message> findLatestConversations(@Param("userId") Long userId);
    
    /**
     * Count unread messages for a user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") Long userId);
    
    /**
     * Find all unread messages for a user
     */
    List<Message> findByReceiverIdAndIsReadFalseOrderBySentAtDesc(Long receiverId);
    
    /**
     * Find all messages where user is sender or receiver
     */
    @Query("SELECT m FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId ORDER BY m.sentAt DESC")
    List<Message> findAllByUserId(@Param("userId") Long userId);
}
