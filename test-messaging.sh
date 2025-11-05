#!/bin/bash
# Test script for messaging system

BASE_URL="http://localhost:8080"

echo "=== Testing Messaging System ==="
echo ""

# Test 1: Send a message from user 1 to user 2 about product 1
echo "1. Sending message from user 1 to user 2..."
curl -X POST "${BASE_URL}/messages/send?senderId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": 2,
    "productId": 1,
    "content": "Hi! Is this iPhone still available?"
  }' | jq '.'
echo ""
echo ""

# Test 2: Get conversations for user 2
echo "2. Getting conversations for user 2..."
curl -X GET "${BASE_URL}/messages/conversations?userId=2" | jq '.'
echo ""
echo ""

# Test 3: Get unread count for user 2
echo "3. Getting unread count for user 2..."
curl -X GET "${BASE_URL}/messages/unread-count?userId=2"
echo ""
echo ""

# Test 4: Get conversation between user 1 and 2 about product 1
echo "4. Getting conversation messages..."
curl -X GET "${BASE_URL}/messages/conversation?userId=2&otherUserId=1&productId=1" | jq '.'
echo ""
echo ""

# Test 5: Send reply from user 2
echo "5. Sending reply from user 2..."
curl -X POST "${BASE_URL}/messages/send?senderId=2" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": 1,
    "productId": 1,
    "content": "Yes! It is still available. Would you like to meet?"
  }' | jq '.'
echo ""
echo ""

# Test 6: Check unread count for user 1 now
echo "6. Getting unread count for user 1..."
curl -X GET "${BASE_URL}/messages/unread-count?userId=1"
echo ""
echo ""

echo "=== Test Complete ==="
