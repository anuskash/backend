-- Migration: Add messaging system tables
-- Date: 2025-11-05
-- Description: Create messages table for buyer-seller communication with email notifications

-- Create messages table
CREATE TABLE messages (
    message_id BIGINT PRIMARY KEY IDENTITY(1,1),
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    content NVARCHAR(2000) NOT NULL,
    sent_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    is_read BIT NOT NULL DEFAULT 0,
    read_at DATETIME2 NULL,
    
    -- Foreign keys
    CONSTRAINT FK_messages_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE NO ACTION,
    CONSTRAINT FK_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE NO ACTION,
    CONSTRAINT FK_messages_product FOREIGN KEY (product_id) REFERENCES marketplace_product(product_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_receiver ON messages(receiver_id);
CREATE INDEX idx_messages_product ON messages(product_id);
CREATE INDEX idx_messages_sent_at ON messages(sent_at DESC);
CREATE INDEX idx_messages_unread ON messages(receiver_id, is_read) WHERE is_read = 0;

-- Composite index for conversation queries
CREATE INDEX idx_messages_conversation ON messages(sender_id, receiver_id, product_id, sent_at);

-- Add comment
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Stores messages between buyers and sellers about specific products. Supports email notifications on new messages.', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'messages';
