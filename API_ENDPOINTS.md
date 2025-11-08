# UoN Marketplace API Endpoints - Frontend Integration Guide

## Base URL
```
http://localhost:8080
```

---

## 🔐 Authentication & User Management

### **POST** `/auth/register`
Register a new user account
```json
Request Body:
{
  "email": "student@uon.edu.au",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "0412345678"
}
```

### **GET** `/auth/login`
User login
```
Query Params: ?email=user@example.com&password=pass123
Response: { "userId": 1, "email": "...", "role": "USER", ... }
```

### **POST** `/auth/login/v2`
Enhanced login with 2FA support
```json
Request Body:
{
  "email": "user@example.com",
  "password": "pass123"
}
Response: { "userId": 1, "requiresTwoFactor": false, ... }
```

### **POST** `/auth/verify-email`
Verify email with OTP
```json
Request Body:
{
  "email": "user@example.com",
  "verificationCode": "ABC123"
}
```

### **GET** `/auth/verification-status`
Check email verification status
```
Query Params: ?email=user@example.com
```

### **POST** `/auth/resend-verification`
Resend verification email
```json
Request Body:
{
  "email": "user@example.com"
}
```

### **POST** `/auth/forgot-password`
Request password reset
```json
Request Body:
{
  "email": "user@example.com"
}
```

### **POST** `/auth/reset-password`
Reset password with token
```json
Request Body:
{
  "token": "reset-token-here",
  "newPassword": "newSecurePass123"
}
```

---

## 🔒 Two-Factor Authentication (2FA)

### **POST** `/auth/2fa/setup`
Setup 2FA for user
```
Headers: userId: <user_id>
Response: { "qrCodeUrl": "...", "secret": "...", "backupCodes": [...] }
```

### **POST** `/auth/2fa/verify`
Verify 2FA code
```json
Headers: userId: <user_id>
Request Body:
{
  "code": "123456"
}
```

### **POST** `/auth/2fa/disable`
Disable 2FA
```json
Headers: userId: <user_id>
Request Body:
{
  "password": "userPassword"
}
```

### **POST** `/auth/2fa/regenerate-backup-codes`
Generate new backup codes
```
Headers: userId: <user_id>
Response: { "backupCodes": ["CODE1", "CODE2", ...] }
```

### **GET** `/auth/2fa/status`
Check 2FA status
```
Headers: userId: <user_id>
Response: { "enabled": true, "hasBackupCodes": true }
```

---

## 👤 User Profile

### **GET** `/users/profile/{userId}`
Get user profile
```
Response: { "userId": 1, "firstName": "John", "lastName": "Doe", ... }
```

### **PUT** `/users/profile/{userId}/picture`
Update profile picture
```
Query Params: ?newProfileImageUrl=http://example.com/pic.jpg
```

### **PUT** `/users/profile/{userId}/phone`
Update phone number
```
Query Params: ?newPhoneNumber=0412345678
```

### **GET** `/users/all/users`
Get all marketplace users
```
Response: [{ "userId": 1, "firstName": "John", "email": "...", ... }]
```

### **GET** `/users/seller-info/{sellerId}`
Get seller information
```
Response: { "userId": 1, "firstName": "John", "email": "...", "rating": 4.5 }
```

---

## 📦 Product Management

### **POST** `/users/product`
Create new product listing
```json
Request Body:
{
  "sellerId": 1,
  "productName": "iPhone 13 Pro",
  "productDescription": "Like new condition",
  "category": "Electronics",
  "condition": "Like New",
  "price": 899.99,
  "imageUrls": ["url1.jpg", "url2.jpg"]
}
Response: MarketPlaceProduct object
```

### **PUT** `/users/product/{productId}`
**🔒 Requires Header: `userId`**
Update product details (full edit)
```json
Headers: userId: <seller_id>
Request Body:
{
  "productName": "Updated Name",
  "productDescription": "Updated description",
  "category": "Electronics",
  "condition": "Like New",
  "price": 799.99,
  "imageUrls": ["new-url1.jpg"]
}
```

### **GET** `/users/products/available`
Get all available products with images
```
Response: [
  {
    "productId": 1,
    "productName": "iPhone 13",
    "price": 899.99,
    "imageUrls": ["url1.jpg", "url2.jpg"],
    "flagged": false,
    ...
  }
]
```

### **GET** `/users/products/seller/{sellerId}`
Get products by seller with images
```
Response: Array of MarketPlaceProductResponse
```

### **GET** `/users/products/buyer/{buyerId}`
Get products purchased by buyer with images
```
Response: Array of MarketPlaceProductResponse
```

### **PUT** `/users/product/{productId}/status`
**🔒 Requires Header: `userId`**
Update product status
```
Headers: userId: <seller_id>
Query Params: ?newStatus=Available
```

### **PUT** `/users/product/{productId}/price`
**🔒 Requires Header: `userId`**
Update product price
```
Headers: userId: <seller_id>
Query Params: ?newPrice=99.99
```

### **PUT** `/users/product/{productId}/sold`
**🔒 Requires Header: `userId`**
Mark product as sold
```
Headers: userId: <seller_id>
Query Params: ?buyerUserId=5
```

### **PUT** `/users/product/{productId}/unavailable`
**🔒 Requires Header: `userId`**
Mark product as unavailable
```
Headers: userId: <seller_id>
```

### **DELETE** `/users/product/{productId}`
**🔒 Requires Header: `userId`**
Delete product
```
Headers: userId: <seller_id>
Response: { "success": true, "message": "Product deleted successfully" }
```

---

## 🖼️ Image Upload

### **POST** `/users/product/upload-image`
Upload single product image
```
Content-Type: multipart/form-data
Form Data: file=<image_file>
Response: { "imageUrl": "http://...", "message": "Image uploaded successfully" }
```

### **POST** `/users/product/upload-multiple-images`
Upload multiple product images (max 10)
```
Content-Type: multipart/form-data
Form Data: files=<image_files[]>
Response: { "imageUrls": ["url1", "url2"], "count": 2 }
```

### **DELETE** `/users/product/delete-image`
Delete product image
```
Query Params: ?imageUrl=http://example.com/image.jpg
Response: { "success": true, "message": "Image deleted successfully" }
```

### **GET** `/users/product/{productId}/images`
Get all images for a product
```
Response: { "productId": 1, "imageUrls": ["url1", "url2"], "count": 2 }
```

### **PUT** `/users/product/{productId}/images`
**🔒 Requires Header: `userId`**
Update product images
```json
Headers: userId: <seller_id>
Request Body:
{
  "imageUrls": ["new-url1.jpg", "new-url2.jpg"]
}
```

---

## ⭐ Reviews

### **GET** `/users/reviews/seller/{sellerId}`
Get all reviews for a seller
```
Response: [{ "reviewId": 1, "rating": 5, "reviewText": "Great seller!", ... }]
```

### **GET** `/users/reviews/reviewer/{reviewerId}`
Get all reviews written by a reviewer
```
Response: Array of SellerReviewResponse
```

### **POST** `/users/reviews/seller`
Add seller review
```json
Request Body:
{
  "sellerId": 1,
  "reviewerId": 2,
  "productId": 10,
  "rating": 5,
  "reviewText": "Excellent seller!"
}
```

### **PUT** `/users/reviews/{reviewId}`
Update seller review
```json
Request Body:
{
  "rating": 4,
  "reviewText": "Updated review text"
}
```

### **GET** `/users/buyer-reviews/buyer/{buyerId}`
Get all reviews for a buyer
```
Response: Array of BuyerReviewResponse
```

### **GET** `/users/buyer-reviews/reviewer/{reviewerId}`
Get buyer reviews written by reviewer
```
Response: Array of BuyerReviewResponse
```

### **POST** `/users/buyer-reviews`
Add buyer review
```json
Request Body:
{
  "buyerId": 1,
  "reviewerId": 2,
  "productId": 10,
  "rating": 5,
  "reviewText": "Great buyer!"
}
```

### **PUT** `/users/buyer-reviews/{reviewId}`
Update buyer review
```json
Request Body:
{
  "rating": 4,
  "reviewText": "Updated review"
}
```

### **GET** `/users/product-reviews/{productId}`
Get all reviews for a specific product
```
Response: {
  "productId": 1,
  "buyerReviews": [...],
  "sellerReviews": [...]
}
```

### **GET** `/users/my-reviews/{userId}`
Get all reviews for a user (given + received)
```
Response: {
  "userId": 1,
  "sellerReviews": [...],
  "buyerReviews": [...],
  "totalReviews": 10,
  "averageSellerRatingReceived": 4.5,
  "averageBuyerRatingReceived": 4.8
}
```

---

## 💬 Messaging

### **POST** `/messages/send`
Send message to another user
```json
Request Body:
{
  "senderId": 1,
  "receiverId": 2,
  "productId": 10,
  "messageText": "Is this still available?"
}
```

### **GET** `/messages/conversations`
Get user's inbox/conversations
```
Query Params: ?userId=1
Response: [
  {
    "conversationId": "1-2-10",
    "otherUserId": 2,
    "otherUserName": "Jane Doe",
    "productId": 10,
    "productName": "iPhone 13",
    "lastMessage": "Yes, it's available",
    "lastMessageTime": "2025-11-07T10:30:00",
    "unreadCount": 2
  }
]
```

### **GET** `/messages/conversation`
Get conversation between two users about a product
```
Query Params: ?userId=1&otherUserId=2&productId=10
Response: {
  "conversationId": "1-2-10",
  "messages": [
    { "messageId": 1, "senderId": 1, "text": "Hi", "timestamp": "...", "isRead": true }
  ]
}
```

### **GET** `/messages/unread-count`
Get unread message count
```
Query Params: ?userId=1
Response: { "unreadCount": 5 }
```

### **PUT** `/messages/{messageId}/mark-read`
Mark message as read
```
Response: Message object
```

---

## 🚩 Reporting & Moderation

### **POST** `/api/reports/product`
**🔒 Requires Header: `userId`**
Report a product
```json
Headers: userId: <reporter_id>
Request Body:
{
  "productId": 10,
  "reportReason": "prohibited_item",
  "reportDetails": "Selling drugs"
}
Response: {
  "success": true,
  "message": "Product reported successfully",
  "reportId": 123
}
```

### **GET** `/api/reports/my-reports`
**🔒 Requires Header: `userId`**
Get user's report history
```
Headers: userId: <user_id>
Response: Array of ProductReport objects
```

---

## 👑 Admin Endpoints (Requires ADMIN/SUPER_ADMIN role)

### Prohibited Keywords Management

**GET** `/admin/prohibited-keywords`
Get all prohibited keywords

**GET** `/admin/prohibited-keywords/category/{category}`
Get keywords by category (drugs, weapons, alcohol, etc.)

**POST** `/admin/prohibited-keywords`
Add new prohibited keyword
```json
Headers: userId: <admin_id>
Request Body:
{
  "keyword": "cocaine",
  "category": "drugs",
  "severity": "high",
  "autoAction": "reject",
  "description": "Illegal substance"
}
```

**DELETE** `/admin/prohibited-keywords/{keywordId}`
Delete prohibited keyword

### Report Moderation

**GET** `/admin/reports/pending`
Get pending product reports (moderation queue)

**POST** `/admin/reports/{reportId}/review`
Review and take action on report
```
Headers: userId: <admin_id>
Query Params: 
  ?action=approved|rejected|remove_product
  &adminNotes=Optional notes
```

### Product Moderation

**GET** `/admin/products/flagged`
Get all flagged products

**POST** `/admin/products/{productId}/unflag`
Remove flag from product (admin override)

### User Management

**PUT** `/admin/verify-user/{userId}`
Verify user account

---

## 📊 Response Objects

### MarketPlaceProductResponse
```json
{
  "productId": 1,
  "sellerId": 1,
  "sellerName": "John Doe",
  "buyerId": null,
  "buyerName": null,
  "productName": "iPhone 13 Pro",
  "category": "Electronics",
  "condition": "Like New",
  "productDescription": "Excellent condition",
  "productImageUrl": "url1.jpg",
  "imageUrls": ["url1.jpg", "url2.jpg"],
  "price": 899.99,
  "postedDate": "2025-11-07T10:30:00",
  "lastUpdate": "2025-11-07T10:30:00",
  "status": "Available",
  "flagged": false,
  "flagReason": null,
  "reportCount": 0
}
```

---

## 🔑 Important Headers

### Authentication Headers (Required for secured endpoints)
```
userId: <user_id>
```

### Content Type
```
Content-Type: application/json
```
(Use `multipart/form-data` for file uploads)

---

## ⚠️ Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "error": "Product rejected: Contains prohibited keyword 'drugs'"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "error": "Unauthorized: You can only edit your own products"
}
```

### 404 Not Found
```json
{
  "success": false,
  "error": "Product not found with ID: 999"
}
```

---

## 🎯 Frontend Integration Tips

1. **Store userId** after login and include in headers for all authenticated requests
2. **Check product ownership** before showing edit/delete buttons (`product.sellerId === currentUserId`)
3. **Handle 403 errors** gracefully (show "Unauthorized" message)
4. **Use detailed endpoints** (`/products/available`) to get products with full image arrays
5. **Implement image upload** before product creation or use upload then attach flow
6. **Poll `/messages/unread-count`** every 30 seconds to show notification badge
7. **Content moderation** is automatic - handle rejection errors when creating/updating products

---

## 🚀 Quick Start Example (TypeScript/React)

```typescript
// Login
const login = async (email: string, password: string) => {
  const response = await fetch('http://localhost:8080/auth/login/v2', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  localStorage.setItem('userId', data.userId);
  return data;
};

// Get available products
const getProducts = async () => {
  const response = await fetch('http://localhost:8080/users/products/available');
  return await response.json();
};

// Create product
const createProduct = async (product: ProductRequest) => {
  const response = await fetch('http://localhost:8080/users/product', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(product)
  });
  return await response.json();
};

// Update product (with ownership check)
const updateProduct = async (productId: number, updates: UpdateRequest) => {
  const userId = localStorage.getItem('userId');
  const response = await fetch(`http://localhost:8080/users/product/${productId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'userId': userId
    },
    body: JSON.stringify(updates)
  });
  if (response.status === 403) {
    throw new Error('You can only edit your own products');
  }
  return await response.json();
};

// Upload images
const uploadImages = async (files: File[]) => {
  const formData = new FormData();
  files.forEach(file => formData.append('files', file));
  
  const response = await fetch('http://localhost:8080/users/product/upload-multiple-images', {
    method: 'POST',
    body: formData
  });
  return await response.json();
};
```
