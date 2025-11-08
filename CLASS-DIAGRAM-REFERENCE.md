# UON Marketplace - Complete Class Diagram Reference

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│                    (REST Controllers)                        │
├─────────────────────────────────────────────────────────────┤
│  AuthController  │  UserController  │  AdminController  │   │
│  MessageController                                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                           │
│                   (Business Logic)                           │
├─────────────────────────────────────────────────────────────┤
│  LoginService           │  AuthenticationService            │
│  UserService            │  AdminService                      │
│  MessageService         │  EmailService                      │
│  TwoFactorAuthService   │  PasswordResetService             │
│  EmailVerificationService                                    │
│  MarketPlaceProductService │ ImageUploadService             │
│  BuyerReviewService     │  SellerReviewService              │
│  UserProfileService     │  JwtService                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                          │
│                (Spring Data JPA Repositories)                │
├─────────────────────────────────────────────────────────────┤
│  AppUserRepository               │  UserProfileRepository    │
│  MarketPlaceProductRepository    │  ProductImageRepository   │
│  BuyerReviewRepository           │  SellerReviewsRepository  │
│  MessageRepository                                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                              │
│                    (JPA Entities)                            │
├─────────────────────────────────────────────────────────────┤
│  AppUser (users table)                                       │
│  UserProfile (user_profiles table)                           │
│  MarketPlaceProduct (marketplace_products table)             │
│  ProductImage (product_images table)                         │
│  BuyerReviews (buyer_reviews table)                          │
│  SellerReviews (seller_reviews table)                        │
│  Message (messages table)                                    │
│  Role (enum: USER, ADMIN, SUPER_ADMIN)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. ENTITIES (Domain Models)

### 1.1 AppUser
**Package:** `com.uon.marketplace.entities`  
**Table:** `users`

**Attributes:**
- `userId` (Long, PK)
- `email` (String, unique)
- `passwordHash` (String)
- `role` (Role enum: USER, ADMIN, SUPER_ADMIN)
- `status` (String: active, banned, inactive)
- `createdAt` (LocalDateTime)
- `emailVerified` (Boolean)
- `emailVerificationCode` (String)
- `emailVerificationExpiresAt` (LocalDateTime)
- `twoFactorEnabled` (Boolean)
- `twoFactorSecret` (String)
- `twoFactorVerifiedAt` (LocalDateTime)
- `twoFactorEmailCode` (String)
- `twoFactorEmailExpiresAt` (LocalDateTime)
- `backupCodes` (String, comma-separated)
- `passwordResetToken` (String)
- `passwordResetExpiresAt` (LocalDateTime)
- `failedLoginAttempts` (Integer)
- `accountLockedUntil` (LocalDateTime)
- `unlockCode` (String)
- `unlockCodeExpiresAt` (LocalDateTime)

**Methods:**
- `incrementFailedAttempts()`
- `resetFailedAttempts()`
- `lockAccount(int minutes)`
- `isAccountLocked(): Boolean`

**Relationships:**
- One-to-One with `UserProfile`
- One-to-Many with `MarketPlaceProduct` (as seller)
- One-to-Many with `BuyerReviews`
- One-to-Many with `SellerReviews`
- One-to-Many with `Message` (as sender/receiver)

---

### 1.2 UserProfile
**Package:** `com.uon.marketplace.entities`  
**Table:** `user_profiles`

**Attributes:**
- `profileId` (Long, PK)
- `userId` (Long, FK to AppUser)
- `firstName` (String)
- `lastName` (String)
- `phoneNumber` (String)
- `profileImageUrl` (String)

**Relationships:**
- One-to-One with `AppUser`

---

### 1.3 MarketPlaceProduct
**Package:** `com.uon.marketplace.entities`  
**Table:** `marketplace_products`

**Attributes:**
- `productId` (Long, PK)
- `sellerId` (Long, FK to AppUser)
- `sellerName` (String)
- `productName` (String)
- `productDescription` (String)
- `price` (BigDecimal)
- `productImageUrl` (String) - Primary/first image
- `category` (String)
- `condition` (String: New, Like New, Good, Fair)
- `status` (String: available, sold, unavailable)
- `postedDate` (LocalDateTime)
- `lastUpdate` (LocalDateTime)
- `buyerId` (Long, FK to AppUser - when sold)
- `buyerName` (String)

**Relationships:**
- Many-to-One with `AppUser` (seller)
- One-to-Many with `ProductImage`
- One-to-Many with `BuyerReviews`
- One-to-Many with `SellerReviews`
- One-to-Many with `Message`

---

### 1.4 ProductImage
**Package:** `com.uon.marketplace.entities`  
**Table:** `product_images`

**Attributes:**
- `imageId` (Long, PK)
- `productId` (Long, FK to MarketPlaceProduct)
- `imageUrl` (String)
- `displayOrder` (Integer) - 0-indexed position
- `isPrimary` (Boolean) - First image = true
- `createdAt` (LocalDateTime)

**Relationships:**
- Many-to-One with `MarketPlaceProduct`

---

### 1.5 BuyerReviews
**Package:** `com.uon.marketplace.entities`  
**Table:** `buyer_reviews`

**Attributes:**
- `reviewId` (Long, PK)
- `buyerId` (Long, FK to AppUser - being reviewed)
- `reviewerId` (Long, FK to AppUser - writer)
- `productId` (Long, FK to MarketPlaceProduct)
- `rating` (Integer, 1-5)
- `reviewText` (String)
- `reviewDate` (LocalDateTime)

**Relationships:**
- Many-to-One with `AppUser` (buyer)
- Many-to-One with `AppUser` (reviewer)
- Many-to-One with `MarketPlaceProduct`

---

### 1.6 SellerReviews
**Package:** `com.uon.marketplace.entities`  
**Table:** `seller_reviews`

**Attributes:**
- `reviewId` (Long, PK)
- `sellerId` (Long, FK to AppUser - being reviewed)
- `reviewerId` (Long, FK to AppUser - writer)
- `productId` (Long, FK to MarketPlaceProduct)
- `rating` (Integer, 1-5)
- `reviewText` (String)
- `reviewDate` (LocalDateTime)

**Relationships:**
- Many-to-One with `AppUser` (seller)
- Many-to-One with `AppUser` (reviewer)
- Many-to-One with `MarketPlaceProduct`

---

### 1.7 Message
**Package:** `com.uon.marketplace.entities`  
**Table:** `messages`

**Attributes:**
- `messageId` (Long, PK)
- `senderId` (Long, FK to AppUser)
- `receiverId` (Long, FK to AppUser)
- `productId` (Long, FK to MarketPlaceProduct)
- `content` (String, text content)
- `sentAt` (LocalDateTime)
- `isRead` (Boolean)
- `readAt` (LocalDateTime)

**Methods:**
- `markAsRead()` - Sets isRead=true and readAt=now

**Relationships:**
- Many-to-One with `AppUser` (sender)
- Many-to-One with `AppUser` (receiver)
- Many-to-One with `MarketPlaceProduct`

---

### 1.8 Role (Enum)
**Package:** `com.uon.marketplace.entities`

**Values:**
- `USER` - Default for registration
- `ADMIN` - Created by SUPER_ADMIN
- `SUPER_ADMIN` - Created manually in DB

---

## 2. REPOSITORIES (Data Access Layer)

### 2.1 AppUserRepository
**Interface:** `JpaRepository<AppUser, Long>`

**Methods:**
- `Optional<AppUser> findByEmail(String email)`
- `Boolean existsByEmail(String email)`
- `List<AppUser> findByRole(Role role)`
- `List<AppUser> findByStatus(String status)`

---

### 2.2 UserProfileRepository
**Interface:** `JpaRepository<UserProfile, Long>`

**Methods:**
- `Optional<UserProfile> findByUserId(Long userId)`
- `void deleteByUserId(Long userId)`

---

### 2.3 MarketPlaceProductRepository
**Interface:** `JpaRepository<MarketPlaceProduct, Long>`

**Methods:**
- `List<MarketPlaceProduct> findBySellerId(Long sellerId)`
- `List<MarketPlaceProduct> findByBuyerId(Long buyerId)`
- `List<MarketPlaceProduct> findByStatus(String status)`
- `List<MarketPlaceProduct> findByCategory(String category)`
- `List<MarketPlaceProduct> findByPriceBetween(BigDecimal min, BigDecimal max)`

---

### 2.4 ProductImageRepository
**Interface:** `JpaRepository<ProductImage, Long>`

**Methods:**
- `List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId)`
- `void deleteByProductId(Long productId)`
- `void deleteByImageUrl(String imageUrl)`
- `Long countByProductId(Long productId)`

---

### 2.5 BuyerReviewRepository
**Interface:** `JpaRepository<BuyerReviews, Long>`

**Methods:**
- `List<BuyerReviews> findByBuyerId(Long buyerId)`
- `List<BuyerReviews> findByReviewerId(Long reviewerId)`
- `List<BuyerReviews> findByProductId(Long productId)`
- `@Query` `Double calculateAverageRating(Long buyerId)`

---

### 2.6 SellerReviewsRepository
**Interface:** `JpaRepository<SellerReviews, Long>`

**Methods:**
- `List<SellerReviews> findBySellerId(Long sellerId)`
- `List<SellerReviews> findByReviewerId(Long reviewerId)`
- `List<SellerReviews> findByProductId(Long productId)`
- `@Query` `Double calculateAverageRating(Long sellerId)`

---

### 2.7 MessageRepository
**Interface:** `JpaRepository<Message, Long>`

**Methods:**
- `@Query` `List<Message> findConversation(Long userId, Long otherUserId, Long productId)` - Get all messages between two users about a product
- `@Query` `List<Message> findLatestConversations(Long userId)` - Get latest message per conversation for inbox
- `@Query` `Long countUnreadMessages(Long receiverId)` - Count unread messages
- `List<Message> findByReceiverIdAndIsReadFalse(Long receiverId)` - Get unread messages

---

## 3. SERVICES (Business Logic Layer)

### 3.1 AuthenticationService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `LoginResponse login(String email, String password)` - Standard login
- `Boolean validateCredentials(String email, String password)` - Check credentials
- `handleFailedLogin(AppUser user)` - Increment attempts, lock if needed
- `handleSuccessfulLogin(AppUser user)` - Reset failed attempts

**Dependencies:**
- AppUserRepository
- PasswordHashUtil
- JwtService

---

### 3.2 LoginService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `AppUserResponse register(CreateUserRequest request)` - User registration
- `AppUser authenticate(String email, String password)` - Legacy login
- `generateJwtToken(AppUser user): String` - Create JWT token

**Dependencies:**
- AppUserRepository
- UserProfileRepository
- PasswordHashUtil
- EmailVerificationService
- JwtService

---

### 3.3 EmailVerificationService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `sendVerificationEmail(AppUser user)` - Send 6-digit code
- `verifyEmail(String email, String code): Boolean` - Verify code
- `resendVerificationCode(String email)` - Resend expired code
- `isVerificationExpired(AppUser user): Boolean` - Check expiry
- `generateVerificationCode(): String` - Create 6-digit code

**Dependencies:**
- AppUserRepository
- EmailService

---

### 3.4 TwoFactorAuthService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `TwoFactorSetupResponse setupTwoFactor(Long userId)` - Generate QR code
- `Boolean verifyTwoFactorCode(Long userId, String code)` - Verify TOTP
- `LoginResponse loginWithTwoFactor(String email, String password, String totpCode)` - 2FA login
- `disableTwoFactor(Long userId, String totpCode)` - Disable 2FA
- `List<String> generateBackupCodes()` - Create recovery codes
- `List<String> regenerateBackupCodes(Long userId)` - New backup codes
- `Boolean verifyBackupCode(Long userId, String code)` - Use backup code

**Dependencies:**
- AppUserRepository
- JwtService
- Google Authenticator library

---

### 3.5 PasswordResetService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `sendPasswordResetEmail(String email)` - Send reset link
- `resetPassword(String token, String newPassword): Boolean` - Reset with token
- `Boolean validateResetToken(String token)` - Check token validity
- `generateResetToken(): String` - Create UUID token

**Dependencies:**
- AppUserRepository
- EmailService
- PasswordHashUtil

---

### 3.6 UserService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `UserProfile getUserProfile(Long userId)`
- `UserProfile changeProfilePicture(Long userId, String imageUrl)`
- `UserProfile updatePhoneNumber(Long userId, String phoneNumber)`
- `MarketPlaceProduct addMarketPlaceProduct(MarketPlaceProductRequest request)`
- `MarketPlaceProduct updateProductStatus(Long productId, String status)`
- `MarketPlaceProduct markProductAsSold(Long productId, Long buyerId)`
- `void removeProduct(Long productId)`
- `List<MarketPlaceProduct> getProductsBySeller(Long sellerId)`
- `List<MarketPlaceProduct> getAllAvailableProducts()`
- `List<ProductImage> getProductImages(Long productId)`

**Dependencies:**
- UserProfileService
- MarketPlaceProductService
- AppUserRepository

---

### 3.7 MarketPlaceProductService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `MarketPlaceProduct createProduct(MarketPlaceProductRequest request)`
- `MarketPlaceProduct updateProduct(Long productId, MarketPlaceProductRequest request)`
- `void deleteProduct(Long productId)`
- `List<MarketPlaceProduct> searchProducts(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice)`
- `void saveProductImages(Long productId, List<String> imageUrls)` - Save multiple images
- `List<ProductImage> getProductImages(Long productId)` - Get all images ordered
- `void updateProductImages(Long productId, List<String> imageUrls)` - Reorder/update images
- `void deleteImageByUrl(String imageUrl)` - Delete specific image

**Dependencies:**
- MarketPlaceProductRepository
- ProductImageRepository

---

### 3.8 ImageUploadService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `String uploadSingleImage(MultipartFile file): String` - Upload one image
- `List<String> uploadMultipleImages(MultipartFile[] files): List<String>` - Upload multiple
- `void deleteImage(String imageUrl)` - Delete file from disk
- `Boolean validateImageFile(MultipartFile file)` - Check type/size
- `String generateUniqueFilename(String originalFilename): String` - UUID-based naming

**Dependencies:**
- File System (uploads/products directory)

---

### 3.9 BuyerReviewService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `BuyerReviewResponse addReview(BuyerReviewRequest request)`
- `BuyerReviewResponse updateReview(Long reviewId, BuyerReviewRequest request)`
- `List<BuyerReviewResponse> getReviewsByBuyerId(Long buyerId)`
- `List<BuyerReviewResponse> getReviewsByReviewerId(Long reviewerId)`
- `Double calculateAverageRating(Long buyerId)`

**Dependencies:**
- BuyerReviewRepository
- AppUserRepository
- MarketPlaceProductRepository

---

### 3.10 SellerReviewService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `SellerReviewResponse addReview(SellerReviewRequest request)`
- `SellerReviewResponse updateReview(Long reviewId, SellerReviewRequest request)`
- `List<SellerReviewResponse> getReviewsBySellerId(Long sellerId)`
- `List<SellerReviewResponse> getReviewsByReviewerId(Long reviewerId)`
- `Double calculateAverageRating(Long sellerId)`

**Dependencies:**
- SellerReviewsRepository
- AppUserRepository
- MarketPlaceProductRepository

---

### 3.11 MessageService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `MessageResponse sendMessage(Long senderId, SendMessageRequest request)` - Send message + email notification
- `List<ConversationResponse> getConversations(Long userId)` - Get inbox/conversation list
- `List<MessageResponse> getConversationMessages(Long userId, Long otherUserId, Long productId)` - Get chat history
- `Long getUnreadCount(Long userId)` - Count unread messages
- `void markAsRead(Long messageId, Long userId)` - Mark message as read
- `convertToMessageResponse(Message message): MessageResponse` - Map to DTO
- `buildConversationResponse(Long userId, Message msg, Long otherUserId): ConversationResponse` - Map to conversation DTO
- `sendMessageNotification(AppUser sender, AppUser receiver, Product product, Message message)` - Email notification

**Dependencies:**
- MessageRepository
- AppUserRepository
- UserProfileRepository
- MarketPlaceProductRepository
- EmailService

---

### 3.12 AdminService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `AppUserResponse createUser(CreateUserRequest request)` - Create USER
- `AppUserResponse createAdmin(CreateUserRequest request)` - Create ADMIN (SUPER_ADMIN only)
- `List<AppUserResponse> getAllUsers()` - Get all users
- `AdminUserProfile getUserProfile(Long userId)` - Get user + profile + reviews
- `AdminUserProfile getUserProfileByEmail(String email)` - Get by email
- `String banUser(Long userId)` - Ban user
- `String unbanUser(Long userId)` - Unban user
- `AppUser verifyUser(Long userId)` - Mark as verified (trust badge)
- `void deleteUser(Long userId)` - Permanently delete (SUPER_ADMIN only)
- `void resetPassword(String email, String newPassword)` - Force password reset

**Dependencies:**
- AppUserRepository
- UserProfileRepository
- BuyerReviewRepository
- SellerReviewsRepository
- PasswordHashUtil

---

### 3.13 EmailService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `void send(String to, String subject, String body)` - Send plain text email
- `void sendHtml(String to, String subject, String htmlBody)` - Send HTML email
- `void sendVerificationEmail(String to, String code)` - Email verification template
- `void sendPasswordResetEmail(String to, String resetLink)` - Password reset template
- `void sendUnlockAccountEmail(String to, String unlockCode)` - Account unlock template
- `void send2FACodeEmail(String to, String code)` - 2FA code template
- `void sendMessageNotification(String to, String senderName, String productName, String message)` - New message template

**Dependencies:**
- JavaMailSender (Spring Mail)
- MailConfig

---

### 3.14 JwtService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `String generateToken(AppUser user): String` - Create JWT with userId, email, role
- `String extractUserId(String token): String` - Get userId from token
- `String extractEmail(String token): String` - Get email from token
- `Role extractRole(String token): Role` - Get role from token
- `Boolean validateToken(String token): Boolean` - Verify signature & expiry
- `Boolean isTokenExpired(String token): Boolean` - Check expiration

**Dependencies:**
- JWT library (io.jsonwebtoken)
- Secret key configuration

---

### 3.15 UserProfileService
**Package:** `com.uon.marketplace.services`

**Methods:**
- `UserProfile createProfile(Long userId, String firstName, String lastName)`
- `UserProfile updateProfile(Long userId, UserProfileRequest request)`
- `UserProfile getProfile(Long userId)`
- `void deleteProfile(Long userId)`

**Dependencies:**
- UserProfileRepository

---

## 4. CONTROLLERS (REST API Layer)

### 4.1 AuthController
**Package:** `com.uon.marketplace.controllers`  
**Base Path:** `/auth`

**Endpoints:**

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| POST | `/login` | Standard login | `email, password` | `AppUser` |
| POST | `/register` | User registration | `CreateUserRequest` | `AppUserResponse` |
| POST | `/verify-email` | Verify email with code | `VerifyEmailRequest` | `{success, message, userId, email}` |
| GET | `/verification-status` | Check verification status | `?email=` | `{emailVerified, codeExpired}` |
| POST | `/resend-verification` | Resend verification code | `ResendVerificationRequest` | `{success, message}` |
| POST | `/login-2fa` | 2FA login | `TwoFactorLoginRequest` | `LoginResponse` |
| POST | `/setup-2fa` | Setup 2FA | `TwoFactorSetupRequest` | `TwoFactorSetupResponse` |
| POST | `/verify-2fa` | Verify 2FA setup | `TwoFactorVerifyRequest` | `{success, message, backupCodes}` |
| POST | `/disable-2fa` | Disable 2FA | `userId, totpCode` | `{success, message}` |
| POST | `/regenerate-backup-codes` | New backup codes | `userId, totpCode` | `{success, backupCodes}` |
| GET | `/2fa-status` | Check 2FA status | `?userId=` | `{twoFactorEnabled, verified}` |
| POST | `/forgot-password` | Request password reset | `ForgotPasswordRequest` | `{success, message}` |
| POST | `/reset-password` | Reset password | `ResetPasswordRequest` | `{success, message}` |
| POST | `/unlock-account` | Unlock account | `UnlockAccountRequest` | `{success, message}` |

**Dependencies:**
- LoginService
- AuthenticationService
- EmailVerificationService
- PasswordResetService

---

### 4.2 UserController
**Package:** `com.uon.marketplace.controllers`  
**Base Path:** `/users`

**Endpoints:**

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| GET | `/profile/{userId}` | Get user profile | Path: userId | `UserProfile` |
| PUT | `/profile/{userId}/picture` | Change profile picture | userId, imageUrl | `UserProfile` |
| PUT | `/profile/{userId}/phone` | Update phone number | userId, phoneNumber | `UserProfile` |
| POST | `/product` | Create product | `MarketPlaceProductRequest` | `MarketPlaceProduct` |
| PUT | `/product/{productId}/status` | Update status | productId, status | `MarketPlaceProduct` |
| PUT | `/product/{productId}/sold` | Mark as sold | productId, buyerId | `MarketPlaceProduct` |
| DELETE | `/product/{productId}` | Delete product | productId | `204 No Content` |
| PUT | `/product/{productId}/unavailable` | Mark unavailable | productId | `MarketPlaceProduct` |
| PUT | `/product/{productId}/price` | Update price | productId, price | `MarketPlaceProduct` |
| GET | `/products/seller/{sellerId}` | Get seller's products | sellerId | `List<MarketPlaceProduct>` |
| GET | `/products/buyer/{buyerId}` | Get buyer's purchases | buyerId | `List<MarketPlaceProduct>` |
| GET | `/products/available` | Get all available | - | `List<MarketPlaceProduct>` |
| POST | `/product/upload-image` | Upload single image | `MultipartFile` | `{imageUrl}` |
| POST | `/product/upload-multiple-images` | Upload multiple images | `MultipartFile[]` | `{imageUrls[], count}` |
| DELETE | `/product/delete-image` | Delete image | `?imageUrl=` | `{success, message}` |
| GET | `/product/{productId}/images` | Get product images | productId | `{productId, imageUrls[], count}` |
| PUT | `/product/{productId}/images` | Update/reorder images | `ProductImagesUpdateRequest` | `{success, message}` |
| GET | `/seller-reviews/{sellerId}` | Get seller reviews | sellerId | `List<SellerReviewResponse>` |
| POST | `/seller-review` | Add seller review | `SellerReviewRequest` | `SellerReviewResponse` |
| PUT | `/seller-review/{reviewId}` | Update seller review | reviewId, request | `SellerReviewResponse` |
| GET | `/buyer-reviews/{buyerId}` | Get buyer reviews | buyerId | `List<BuyerReviewResponse>` |
| POST | `/buyer-review` | Add buyer review | `BuyerReviewRequest` | `BuyerReviewResponse` |
| PUT | `/buyer-review/{reviewId}` | Update buyer review | reviewId, request | `BuyerReviewResponse` |
| GET | `/all-users` | Get all users | - | `List<MarketPlaceUser>` |
| GET | `/product-reviews/{productId}` | Get product reviews | productId | `ProductReviews` |
| GET | `/my-reviews/{userId}` | Get user's reviews | userId | `MyReviews` |
| GET | `/seller-info/{sellerId}` | Get seller info | sellerId | `MarketPlaceUser` |

**Dependencies:**
- UserService
- ImageUploadService

---

### 4.3 AdminController
**Package:** `com.uon.marketplace.controllers`  
**Base Path:** `/admin`  
**Security:** `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`

**Endpoints:**

| Method | Path | Description | Security | Request | Response |
|--------|------|-------------|----------|---------|----------|
| POST | `/create-user` | Create regular user | ADMIN+ | `CreateUserRequest` | `AppUserResponse` |
| POST | `/create-admin` | Create admin | **SUPER_ADMIN** | `CreateUserRequest` | `AppUserResponse` |
| GET | `/users` | Get all users | ADMIN+ | - | `List<AppUserResponse>` |
| GET | `/user-profile/{userId}` | Get user profile | ADMIN+ | userId | `AdminUserProfile` |
| GET | `/user-profile/by-email` | Get user by email | ADMIN+ | `?email=` | `AdminUserProfile` |
| GET | `/buyer-reviews/{userId}` | Get buyer reviews | ADMIN+ | userId | `List<BuyerReviewResponse>` |
| GET | `/seller-reviews/{userId}` | Get seller reviews | ADMIN+ | userId | `List<SellerReviewResponse>` |
| POST | `/ban-user/{userId}` | Ban user | ADMIN+ | userId | `{message}` |
| POST | `/unban-user/{userId}` | Unban user | ADMIN+ | userId | `{message}` |
| PUT | `/verify-user/{userId}` | Verify user (trust badge) | ADMIN+ | userId | `AppUser` |
| POST | `/reset-password` | Force password reset | ADMIN+ | email, newPassword | `{message}` |
| DELETE | `/delete-user/{userId}` | Permanently delete user | **SUPER_ADMIN** | userId | `{message}` |

**Dependencies:**
- AdminService

---

### 4.4 MessageController
**Package:** `com.uon.marketplace.controllers`  
**Base Path:** `/messages`

**Endpoints:**

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| POST | `/send` | Send message | `?senderId=`, `SendMessageRequest` | `MessageResponse` |
| GET | `/conversations` | Get inbox | `?userId=` | `List<ConversationResponse>` |
| GET | `/conversation` | Get chat messages | `?userId=&otherUserId=&productId=` | `List<MessageResponse>` |
| GET | `/unread-count` | Get unread count | `?userId=` | `Long` |
| PUT | `/{messageId}/mark-read` | Mark as read | messageId, `?userId=` | `{message}` |

**Dependencies:**
- MessageService

---

## 5. DTOs (Data Transfer Objects)

### 5.1 Request DTOs

| Class | Package | Fields | Used By |
|-------|---------|--------|---------|
| `CreateUserRequest` | `dto.requests` | email, password, firstName, lastName, phoneNumber | Auth, Admin |
| `LoginRequest` | `dto.requests` | email, password | Auth |
| `VerifyEmailRequest` | `dto.requests` | email, code | Auth |
| `ResendVerificationRequest` | `dto.requests` | email | Auth |
| `TwoFactorLoginRequest` | `dto.requests` | email, password, totpCode | Auth |
| `TwoFactorSetupRequest` | `dto.requests` | userId | Auth |
| `TwoFactorVerifyRequest` | `dto.requests` | userId, totpCode | Auth |
| `ForgotPasswordRequest` | `dto.requests` | email | Auth |
| `ResetPasswordRequest` | `dto.requests` | token, newPassword | Auth |
| `UnlockAccountRequest` | `dto.requests` | email, unlockCode | Auth |
| `MarketPlaceProductRequest` | `dto.requests` | sellerId, productName, productDescription, price, condition, category, imageUrls (List) | User |
| `ProductImagesUpdateRequest` | `dto.requests` | imageUrls (List) | User |
| `SellerReviewRequest` | `dto.requests` | sellerId, reviewerId, productId, rating, reviewText | User |
| `BuyerReviewRequest` | `dto.requests` | buyerId, reviewerId, productId, rating, reviewText | User |
| `UserProfileRequest` | `dto.requests` | firstName, lastName, phoneNumber, profileImageUrl | User |
| `SendMessageRequest` | `dto.requests` | receiverId, productId, content | Message |

---

### 5.2 Response DTOs

| Class | Package | Fields | Returned By |
|-------|---------|--------|-------------|
| `AppUserResponse` | `dto.responses` | userId, email, role, status, emailVerified, createdAt | Auth, Admin |
| `LoginResponse` | `dto.responses` | userId, email, role, token, requires2FA, message | Auth |
| `TwoFactorSetupResponse` | `dto.responses` | qrCodeUrl, secret, backupCodes | Auth |
| `AdminUserProfile` | `dto.responses` | user (AppUserResponse), profile (UserProfile), buyerReviews, sellerReviews | Admin |
| `MessageResponse` | `dto.responses` | messageId, senderId, senderName, receiverId, receiverName, productId, productTitle, productImageUrl, content, sentAt, isRead, readAt | Message |
| `ConversationResponse` | `dto.responses` | otherUserId, otherUserName, productId, productTitle, productImageUrl, lastMessage, lastMessageTime, hasUnread, unreadCount | Message |
| `SellerReviewResponse` | `dto.responses` | reviewId, sellerId, reviewerId, productId, rating, reviewText, reviewDate | User |
| `BuyerReviewResponse` | `dto.responses` | reviewId, buyerId, reviewerId, productId, rating, reviewText, reviewDate | User |
| `ProductReviews` | `dto.responses` | buyerReviews, sellerReviews | User |
| `MyReviews` | `dto.responses` | reviewsIWrote, reviewsIReceived | User |
| `MarketPlaceUser` | `dto.responses` | userId, email, firstName, lastName, phoneNumber, profileImageUrl, averageRating | User |
| `AverageRating` | `dto.responses` | averageRating, totalReviews | User |

---

## 6. UTILITIES & CONFIGURATION

### 6.1 PasswordHashUtil
**Package:** `com.uon.marketplace.utils`

**Methods:**
- `String hashPassword(String plainPassword): String` - Hash with BCrypt
- `Boolean verifyPassword(String plainPassword, String hashedPassword): Boolean` - Verify BCrypt hash

---

### 6.2 ResponseMapper
**Package:** `com.uon.marketplace.utils`

**Methods:**
- `AppUserResponse toAppUserResponse(AppUser user): AppUserResponse`
- `MessageResponse toMessageResponse(Message message): MessageResponse`
- (Add other DTO mapping methods as needed)

---

### 6.3 SecurityConfig
**Package:** `com.uon.marketplace.config`

**Configuration:**
- CORS enabled
- All endpoints `.permitAll()` (development mode)
- `@EnableMethodSecurity` for `@PreAuthorize` annotations
- Password encoder (BCrypt)

**Future:** JWT filter chain for production

---

### 6.4 MailConfig
**Package:** `com.uon.marketplace.config`

**Configuration:**
- SMTP settings
- Gmail integration
- Email templates

---

### 6.5 GlobalExceptionHandler
**Package:** `com.uon.marketplace.config`

**Handles:**
- `DuplicateEmailException`
- `ResourceNotFoundException`
- `UnauthorizedException`
- `ValidationException`
- Generic exceptions

---

### 6.6 CorsConfig
**Package:** `com.uon.marketplace.config`

**Configuration:**
- Allow all origins (development)
- Allow credentials
- Exposed headers

---

## 7. EXCEPTIONS

### 7.1 DuplicateEmailException
**Package:** `com.uon.marketplace.exceptions`

Thrown when email already exists during registration.

---

## 8. DATABASE RELATIONSHIPS SUMMARY

```
AppUser (1) ←→ (1) UserProfile
AppUser (1) ←→ (*) MarketPlaceProduct (as seller)
AppUser (1) ←→ (*) MarketPlaceProduct (as buyer)
AppUser (1) ←→ (*) BuyerReviews (as buyer)
AppUser (1) ←→ (*) BuyerReviews (as reviewer)
AppUser (1) ←→ (*) SellerReviews (as seller)
AppUser (1) ←→ (*) SellerReviews (as reviewer)
AppUser (1) ←→ (*) Message (as sender)
AppUser (1) ←→ (*) Message (as receiver)

MarketPlaceProduct (1) ←→ (*) ProductImage
MarketPlaceProduct (1) ←→ (*) BuyerReviews
MarketPlaceProduct (1) ←→ (*) SellerReviews
MarketPlaceProduct (1) ←→ (*) Message
```

---

## 9. KEY WORKFLOWS

### 9.1 User Registration & Login
```
1. POST /auth/register → LoginService.register()
   ↓
2. AppUser created (role=USER, emailVerified=false)
   ↓
3. EmailVerificationService.sendVerificationEmail()
   ↓
4. User receives 6-digit code
   ↓
5. POST /auth/verify-email → EmailVerificationService.verifyEmail()
   ↓
6. emailVerified=true
   ↓
7. POST /auth/login → AuthenticationService.login()
   ↓
8. LoginResponse with JWT token
```

### 9.2 Product Creation with Multiple Images
```
1. POST /users/product/upload-multiple-images
   ↓
2. ImageUploadService.uploadMultipleImages()
   ↓
3. Files saved to disk, URLs returned
   ↓
4. POST /users/product (with imageUrls array)
   ↓
5. UserService.addMarketPlaceProduct()
   ↓
6. MarketPlaceProductService.createProduct()
   ↓
7. MarketPlaceProductService.saveProductImages()
   ↓
8. ProductImage records created with displayOrder
```

### 9.3 Messaging Flow
```
1. POST /messages/send?senderId=1
   ↓
2. MessageService.sendMessage()
   ↓
3. Message entity saved
   ↓
4. EmailService sends notification to receiver
   ↓
5. GET /messages/conversations?userId=2
   ↓
6. MessageService.getConversations()
   ↓
7. List of conversations with unread counts
   ↓
8. GET /messages/conversation?userId=2&otherUserId=1&productId=10
   ↓
9. MessageService.getConversationMessages()
   ↓
10. Messages auto-marked as read
```

---

## 10. TECHNOLOGY STACK

- **Framework:** Spring Boot 3.5.7
- **Database:** Microsoft SQL Server
- **ORM:** Hibernate/JPA
- **Security:** Spring Security (with @PreAuthorize)
- **Authentication:** JWT (planned), BCrypt password hashing
- **Email:** JavaMailSender (Gmail SMTP)
- **2FA:** Google Authenticator (TOTP)
- **File Upload:** Multipart file handling
- **API Documentation:** Swagger/OpenAPI 3.0

---

## 11. COMPLETE CLASS LIST

**Entities (8):**
1. AppUser
2. UserProfile
3. MarketPlaceProduct
4. ProductImage
5. BuyerReviews
6. SellerReviews
7. Message
8. Role (enum)

**Repositories (7):**
1. AppUserRepository
2. UserProfileRepository
3. MarketPlaceProductRepository
4. ProductImageRepository
5. BuyerReviewRepository
6. SellerReviewsRepository
7. MessageRepository

**Services (15):**
1. AuthenticationService
2. LoginService
3. EmailVerificationService
4. TwoFactorAuthService
5. PasswordResetService
6. UserService
7. MarketPlaceProductService
8. ImageUploadService
9. BuyerReviewService
10. SellerReviewService
11. MessageService
12. AdminService
13. EmailService
14. JwtService
15. UserProfileService

**Controllers (4):**
1. AuthController
2. UserController
3. AdminController
4. MessageController

**Request DTOs (15):**
1. CreateUserRequest
2. LoginRequest
3. VerifyEmailRequest
4. ResendVerificationRequest
5. TwoFactorLoginRequest
6. TwoFactorSetupRequest
7. TwoFactorVerifyRequest
8. ForgotPasswordRequest
9. ResetPasswordRequest
10. UnlockAccountRequest
11. MarketPlaceProductRequest
12. ProductImagesUpdateRequest
13. SellerReviewRequest
14. BuyerReviewRequest
15. SendMessageRequest

**Response DTOs (12):**
1. AppUserResponse
2. LoginResponse
3. TwoFactorSetupResponse
4. AdminUserProfile
5. MessageResponse
6. ConversationResponse
7. SellerReviewResponse
8. BuyerReviewResponse
9. ProductReviews
10. MyReviews
11. MarketPlaceUser
12. AverageRating

**Config Classes (6):**
1. SecurityConfig
2. CorsConfig
3. MailConfig
4. WebConfig
5. GlobalExceptionHandler
6. ServletInitializer

**Utilities (2):**
1. PasswordHashUtil
2. ResponseMapper

**Exceptions (1):**
1. DuplicateEmailException

---

**TOTAL CLASSES: 70+**

Use this reference to create your class diagram in tools like:
- draw.io
- Lucidchart
- PlantUML
- Visual Paradigm
- StarUML
