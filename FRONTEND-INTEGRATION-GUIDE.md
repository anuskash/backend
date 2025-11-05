# Frontend Integration Guide - UON Marketplace
**Complete API Integration Reference for Angular Frontend**

Last Updated: November 5, 2025  
Backend Version: Spring Boot 3.5.7  
Base URL: `http://localhost:8080`

---

## Table of Contents
1. [Overview & Breaking Changes](#overview--breaking-changes)
2. [Authentication & Registration](#1-authentication--registration)
3. [Email Verification](#2-email-verification)
4. [Two-Factor Authentication (2FA)](#3-two-factor-authentication-2fa)
5. [Password Reset & Account Unlock](#4-password-reset--account-unlock)
6. [Role-Based Access Control (RBAC)](#5-role-based-access-control-rbac)
7. [Product Management](#6-product-management)
8. [Image Uploads](#7-image-uploads)
9. [In-App Messaging](#8-in-app-messaging)
10. [Error Handling](#9-error-handling)
11. [Integration Checklist](#10-integration-checklist)

---

## Overview & Breaking Changes

### What's New
✅ Email verification required for new registrations  
✅ Optional TOTP-based 2FA with QR codes  
✅ Password reset via email codes  
✅ Account locking after failed login attempts  
✅ Three-tier role system (USER, ADMIN, SUPER_ADMIN)  
✅ In-app messaging between buyers/sellers  
✅ Image upload for products (single & multiple)  
✅ Email notifications for messages

### Breaking Changes
⚠️ **Login Flow**: Email verification step added after registration  
⚠️ **Login Response**: Now includes `twoFactorRequired` flag  
⚠️ **JWT Tokens**: Generated but not enforced yet (method security disabled for testing)  
⚠️ **Role Field**: Changed from String to Enum (USER, ADMIN, SUPER_ADMIN)

### Migration Path
1. Update registration flow to handle email verification
2. Add 2FA setup UI (optional feature)
3. Implement password reset flow
4. Add role-based route guards
5. Integrate messaging components
6. Update product form for image uploads

---

## 1. Authentication & Registration

### 1.1 User Registration
**Endpoint**: `POST /auth/register`

**Request**:
```json
{
  "appUser": {
    "email": "user@example.com",
    "password": "SecurePass123!"
  },
  "userProfile": {
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "0401234567",
    "profileImageUrl": "https://example.com/profile.jpg"
  }
}
```

**Success Response** (201):
```json
{
  "message": "Registration successful. Please check your email to verify your account.",
  "userId": 123,
  "email": "user@example.com",
  "verificationEmailSent": true
}
```

**Error Response** (409 - Duplicate Email):
```json
{
  "timestamp": "2025-11-05T08:00:00.000+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "path": "/auth/register"
}
```

**Angular Service**:
```typescript
// auth.service.ts
export interface RegisterRequest {
  appUser: { email: string; password: string };
  userProfile: {
    firstName: string;
    lastName: string;
    phoneNumber: string;
    profileImageUrl?: string;
  };
}

register(data: RegisterRequest): Observable<any> {
  return this.http.post(`${this.baseUrl}/auth/register`, data);
}
```

**Component Integration**:
```typescript
// register.component.ts
onSubmit() {
  this.authService.register(this.registerForm.value).subscribe({
    next: (response) => {
      this.showSuccess('Registration successful! Check your email for verification code.');
      this.router.navigate(['/verify-email'], { 
        queryParams: { email: this.registerForm.value.appUser.email } 
      });
    },
    error: (err) => {
      if (err.status === 409) {
        this.showError('Email already registered. Please use a different email.');
      } else {
        this.showError('Registration failed. Please try again.');
      }
    }
  });
}
```

---

## 2. Email Verification

### 2.1 Verify Email
**Endpoint**: `POST /auth/verify-email`

**Request**:
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Success Response** (200):
```json
{
  "message": "Email verified successfully",
  "email": "user@example.com",
  "verified": true
}
```

**Error Response** (400):
```json
{
  "message": "Invalid or expired verification code",
  "verified": false
}
```

### 2.2 Resend Verification Code
**Endpoint**: `POST /auth/resend-verification`

**Request**:
```json
{
  "email": "user@example.com"
}
```

**Success Response** (200):
```json
{
  "message": "Verification code resent successfully",
  "email": "user@example.com"
}
```

**Angular Component**:
```typescript
// verify-email.component.ts
export class VerifyEmailComponent {
  email: string;
  verificationCode: string = '';

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'];
    });
  }

  verify() {
    this.authService.verifyEmail(this.email, this.verificationCode).subscribe({
      next: () => {
        this.showSuccess('Email verified! You can now log in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.showError('Invalid or expired code. Please try again.');
      }
    });
  }

  resendCode() {
    this.authService.resendVerificationCode(this.email).subscribe({
      next: () => {
        this.showSuccess('New verification code sent to your email.');
      },
      error: () => {
        this.showError('Failed to resend code.');
      }
    });
  }
}
```

---

## 3. Two-Factor Authentication (2FA)

### 3.1 Login Flow (Updated)
**Endpoint**: `POST /auth/login/v2`

**Request**:
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Success Response** (200 - No 2FA):
```json
{
  "userId": 123,
  "email": "user@example.com",
  "role": "USER",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "twoFactorRequired": false,
  "twoFactorEnabled": false,
  "emailVerified": true
}
```

**Success Response** (200 - 2FA Required):
```json
{
  "userId": 123,
  "email": "user@example.com",
  "twoFactorRequired": true,
  "twoFactorEnabled": true,
  "message": "Please enter your 2FA code"
}
```

**Error Response** (401):
```json
{
  "message": "Invalid email or password"
}
```

**Error Response** (403 - Email Not Verified):
```json
{
  "message": "Please verify your email before logging in",
  "emailVerified": false
}
```

**Error Response** (423 - Account Locked):
```json
{
  "message": "Account locked due to multiple failed login attempts. Check your email for unlock code.",
  "accountLocked": true,
  "lockedUntil": "2025-11-05T09:00:00"
}
```

### 3.2 Setup 2FA
**Endpoint**: `POST /auth/2fa/setup`

**Request**:
```json
{
  "userId": 123
}
```

**Success Response** (200):
```json
{
  "secretKey": "JBSWY3DPEHPK3PXP",
  "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "backupCodes": [
    "12345678",
    "87654321",
    "11223344",
    "44332211",
    "55667788",
    "88776655",
    "99887766",
    "66778899",
    "22334455",
    "55443322"
  ],
  "message": "Scan QR code with authenticator app"
}
```

### 3.3 Verify 2FA Setup
**Endpoint**: `POST /auth/2fa/verify`

**Request**:
```json
{
  "userId": 123,
  "code": "123456"
}
```

**Success Response** (200):
```json
{
  "message": "Two-factor authentication enabled successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 123,
  "email": "user@example.com",
  "role": "USER"
}
```

### 3.4 Login with 2FA Code
**Endpoint**: `POST /auth/2fa/verify` (same endpoint, different flow)

**Request**:
```json
{
  "userId": 123,
  "code": "123456"
}
```

**Success Response** (200):
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 123,
  "email": "user@example.com",
  "role": "USER"
}
```

**Angular Login Flow**:
```typescript
// login.component.ts
export class LoginComponent {
  step: 'credentials' | '2fa' = 'credentials';
  userId: number;

  login() {
    this.authService.login(this.email, this.password).subscribe({
      next: (response) => {
        if (response.twoFactorRequired) {
          // Show 2FA input
          this.step = '2fa';
          this.userId = response.userId;
        } else {
          // Direct login success
          this.storeToken(response.token);
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        if (err.status === 403 && !err.error.emailVerified) {
          this.showError('Please verify your email first.');
          this.router.navigate(['/verify-email'], { 
            queryParams: { email: this.email } 
          });
        } else if (err.status === 423) {
          this.showError('Account locked. Check your email for unlock code.');
          this.router.navigate(['/unlock-account'], { 
            queryParams: { email: this.email } 
          });
        } else {
          this.showError('Invalid credentials.');
        }
      }
    });
  }

  verify2FA() {
    this.authService.verify2FA(this.userId, this.twoFactorCode).subscribe({
      next: (response) => {
        this.storeToken(response.token);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.showError('Invalid 2FA code.');
      }
    });
  }
}
```

---

## 4. Password Reset & Account Unlock

### 4.1 Forgot Password
**Endpoint**: `POST /auth/forgot-password`

**Request**:
```json
{
  "email": "user@example.com"
}
```

**Success Response** (200):
```json
{
  "message": "Password reset code sent to your email",
  "email": "user@example.com"
}
```

### 4.2 Reset Password
**Endpoint**: `POST /auth/reset-password`

**Request**:
```json
{
  "email": "user@example.com",
  "resetCode": "123456",
  "newPassword": "NewSecurePass123!"
}
```

**Success Response** (200):
```json
{
  "message": "Password reset successfully",
  "email": "user@example.com"
}
```

### 4.3 Unlock Account
**Endpoint**: `POST /auth/unlock-account`

**Request**:
```json
{
  "email": "user@example.com",
  "unlockCode": "123456"
}
```

**Success Response** (200):
```json
{
  "message": "Account unlocked successfully",
  "email": "user@example.com"
}
```

**Angular Password Reset Flow**:
```typescript
// forgot-password.component.ts
export class ForgotPasswordComponent {
  step: 'email' | 'reset' = 'email';
  email: string;

  sendResetCode() {
    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.showSuccess('Reset code sent to your email.');
        this.step = 'reset';
      },
      error: () => {
        this.showError('Failed to send reset code.');
      }
    });
  }

  resetPassword() {
    this.authService.resetPassword(
      this.email, 
      this.resetCode, 
      this.newPassword
    ).subscribe({
      next: () => {
        this.showSuccess('Password reset successfully!');
        this.router.navigate(['/login']);
      },
      error: () => {
        this.showError('Invalid or expired reset code.');
      }
    });
  }
}
```

---

## 5. Role-Based Access Control (RBAC)

### Role Hierarchy
- **USER**: Regular users (default) - manage own profile, products, reviews
- **ADMIN**: User management, ban/unban, view all profiles, reset passwords
- **SUPER_ADMIN**: Full access including creating admins and deleting users

### 5.1 Admin Endpoints

**Get All Users** (ADMIN, SUPER_ADMIN):
```typescript
GET /admin/users
Response: MarketPlaceUser[]
```

**Get User Profile by Email** (ADMIN, SUPER_ADMIN):
```typescript
GET /admin/user-profile/by-email?email=user@example.com
Response: AdminUserProfile
```

**Create Admin User** (SUPER_ADMIN only):
```typescript
POST /admin/create-admin
Request: {
  "appUser": { "email": "admin@example.com", "password": "AdminPass123!" },
  "userProfile": { "firstName": "Admin", "lastName": "User", "phoneNumber": "0401234567" }
}
Response: AppUserResponse
```

**Delete User** (SUPER_ADMIN only):
```typescript
DELETE /admin/delete-user/{userId}
Response: 204 No Content
```

**Ban User** (ADMIN, SUPER_ADMIN):
```typescript
POST /admin/ban-user?userId=123
Response: AppUser
```

**Unban User** (ADMIN, SUPER_ADMIN):
```typescript
POST /admin/unban-user?userId=123
Response: AppUser
```

### 5.2 Angular Route Guards

```typescript
// admin.guard.ts
@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const role = this.authService.getUserRole();
    if (role === 'ADMIN' || role === 'SUPER_ADMIN') {
      return true;
    }
    this.router.navigate(['/unauthorized']);
    return false;
  }
}

// super-admin.guard.ts
@Injectable({ providedIn: 'root' })
export class SuperAdminGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const role = this.authService.getUserRole();
    if (role === 'SUPER_ADMIN') {
      return true;
    }
    this.router.navigate(['/unauthorized']);
    return false;
  }
}

// routing module
const routes: Routes = [
  {
    path: 'admin',
    canActivate: [AdminGuard],
    children: [
      { path: 'users', component: UserManagementComponent },
      { path: 'reports', component: ReportsComponent }
    ]
  },
  {
    path: 'super-admin',
    canActivate: [SuperAdminGuard],
    children: [
      { path: 'create-admin', component: CreateAdminComponent },
      { path: 'delete-user', component: DeleteUserComponent }
    ]
  }
];
```

### 5.3 Role-Based UI Rendering

```typescript
// navbar.component.ts
export class NavbarComponent {
  isAdmin = false;
  isSuperAdmin = false;

  ngOnInit() {
    const role = this.authService.getUserRole();
    this.isAdmin = role === 'ADMIN' || role === 'SUPER_ADMIN';
    this.isSuperAdmin = role === 'SUPER_ADMIN';
  }
}
```

```html
<!-- navbar.component.html -->
<nav>
  <a routerLink="/products">Products</a>
  <a routerLink="/messages">Messages</a>
  
  <a *ngIf="isAdmin" routerLink="/admin/users">User Management</a>
  <a *ngIf="isSuperAdmin" routerLink="/super-admin/create-admin">Create Admin</a>
</nav>
```

---

## 6. Product Management

### 6.1 Create Product
**Endpoint**: `POST /users/product`

**Request**:
```json
{
  "productName": "iPhone 13",
  "description": "Like new, 128GB",
  "price": 699.99,
  "sellerId": 123,
  "category": "Electronics",
  "condition": "Like New",
  "status": "AVAILABLE",
  "imageUrls": [
    "http://localhost:8080/uploads/products/image1.jpg",
    "http://localhost:8080/uploads/products/image2.jpg"
  ]
}
```

**Success Response** (200):
```json
{
  "productId": 456,
  "sellerId": 123,
  "sellerName": "John Doe",
  "productName": "iPhone 13",
  "category": "Electronics",
  "condition": "Like New",
  "productDescription": "Like new, 128GB",
  "productImageUrl": "http://localhost:8080/uploads/products/image1.jpg",
  "price": 699.99,
  "postedDate": "2025-11-05T08:00:00",
  "lastUpdate": "2025-11-05T08:00:00",
  "status": "available"
}
```

### 6.2 Get Available Products
**Endpoint**: `GET /users/products/available`

**Response**: Array of MarketPlaceProduct

### 6.3 Get Products by Seller
**Endpoint**: `GET /users/products/seller/{sellerId}`

**Response**: Array of MarketPlaceProduct

---

## 7. Image Uploads

### 7.1 Upload Single Image
**Endpoint**: `POST /users/product/upload-image`

**Request**: `multipart/form-data`
- Field name: `file`
- Accepted formats: JPEG, PNG, WEBP
- Max size: 5MB

**Success Response** (200):
```json
{
  "imageUrl": "http://localhost:8080/uploads/products/1730794123456_image.jpg",
  "message": "Image uploaded successfully"
}
```

**Error Response** (400):
```json
{
  "error": "Invalid file type. Only JPEG, PNG, and WEBP are allowed."
}
```

### 7.2 Upload Multiple Images
**Endpoint**: `POST /users/product/upload-multiple-images`

**Request**: `multipart/form-data`
- Field name: `files` (array)
- Max files: 10
- Max size per file: 5MB

**Success Response** (200):
```json
{
  "imageUrls": [
    "http://localhost:8080/uploads/products/1730794123456_image1.jpg",
    "http://localhost:8080/uploads/products/1730794123457_image2.jpg"
  ],
  "message": "Images uploaded successfully",
  "count": 2
}
```

Tip: In Swagger UI, make sure you actually attach multiple files to the same field name `files`.
- Use the file picker to select multiple files at once (Cmd/Ctrl + click), or
- Click “Add file” to add another `files` part.

cURL example with two files:

```bash
curl -X POST "http://localhost:8080/users/product/upload-multiple-images" \
  -H "accept: application/json" \
  -H "Content-Type: multipart/form-data" \
  -F "files=@/path/to/img1.jpg;type=image/jpeg" \
  -F "files=@/path/to/img2.jpg;type=image/jpeg"
```

### 7.3 Delete Image
**Endpoint**: `DELETE /users/product/delete-image?imageUrl=http://localhost:8080/uploads/products/image.jpg`

**Success Response** (200):
```json
{
  "success": true,
  "message": "Image deleted successfully"
}
```

### Angular Image Upload Component

```typescript
// product-form.component.ts
export class ProductFormComponent {
  selectedFiles: File[] = [];
  uploadedImageUrls: string[] = [];

  onFilesSelected(event: any) {
    this.selectedFiles = Array.from(event.target.files);
  }

  uploadImages() {
    const formData = new FormData();
    this.selectedFiles.forEach(file => {
      formData.append('files', file);
    });

    this.productService.uploadImages(formData).subscribe({
      next: (response) => {
        this.uploadedImageUrls = response.imageUrls;
        this.showSuccess(`${response.count} images uploaded successfully`);
      },
      error: (err) => {
        this.showError('Image upload failed: ' + err.error.error);
      }
    });
  }

  deleteImage(imageUrl: string) {
    this.productService.deleteImage(imageUrl).subscribe({
      next: () => {
        this.uploadedImageUrls = this.uploadedImageUrls.filter(url => url !== imageUrl);
        this.showSuccess('Image deleted');
      },
      error: () => {
        this.showError('Failed to delete image');
      }
    });
  }

  submitProduct() {
    const productData = {
      ...this.productForm.value,
      imageUrls: this.uploadedImageUrls
    };

    this.productService.createProduct(productData).subscribe({
      next: () => {
        this.showSuccess('Product listed successfully');
        this.router.navigate(['/my-products']);
      },
      error: () => {
        this.showError('Failed to create product');
      }
    });
  }
}
```

### 7.4 Get Product Images
**Endpoint**: `GET /users/product/{productId}/images`

**Success Response** (200):
```json
{
  "productId": 456,
  "imageUrls": [
    "http://localhost:8080/uploads/products/1730794123456_image1.jpg",
    "http://localhost:8080/uploads/products/1730794123457_image2.jpg",
    "http://localhost:8080/uploads/products/1730794123458_image3.jpg"
  ],
  "count": 3
}
```

**Angular Service**:
```typescript
// product.service.ts
getProductImages(productId: number): Observable<any> {
  return this.http.get(`${this.baseUrl}/users/product/${productId}/images`);
}
```

### 7.5 Update Product Images
**Endpoint**: `PUT /users/product/{productId}/images`

**Request**:
```json
{
  "imageUrls": [
    "http://localhost:8080/uploads/products/image1.jpg",
    "http://localhost:8080/uploads/products/image2.jpg"
  ]
}
```

**Success Response** (200):
```json
{
  "productId": 456,
  "updated": true,
  "count": 2
}
```

**Angular Service**:
```typescript
// product.service.ts
updateProductImages(productId: number, imageUrls: string[]): Observable<any> {
  return this.http.put(`${this.baseUrl}/users/product/${productId}/images`, { imageUrls });
}
```

### 7.6 Complete Product Details with Image Gallery

```typescript
// product-details.component.ts
export class ProductDetailsComponent implements OnInit {
  product: any;
  productImages: string[] = [];
  selectedImageIndex = 0;

  ngOnInit() {
    const productId = this.route.snapshot.params['id'];
    
    // Load product details
    this.productService.getProduct(productId).subscribe({
      next: (product) => {
        this.product = product;
        this.loadProductImages(productId);
      }
    });
  }

  loadProductImages(productId: number) {
    this.productService.getProductImages(productId).subscribe({
      next: (response) => {
        this.productImages = response.imageUrls;
        // If no images in product_images table, fall back to single productImageUrl
        if (this.productImages.length === 0 && this.product.productImageUrl) {
          this.productImages = [this.product.productImageUrl];
        }
      },
      error: () => {
        // Fallback to single image from product
        if (this.product.productImageUrl) {
          this.productImages = [this.product.productImageUrl];
        }
      }
    });
  }

  selectImage(index: number) {
    this.selectedImageIndex = index;
  }

  nextImage() {
    if (this.selectedImageIndex < this.productImages.length - 1) {
      this.selectedImageIndex++;
    } else {
      this.selectedImageIndex = 0;
    }
  }

  previousImage() {
    if (this.selectedImageIndex > 0) {
      this.selectedImageIndex--;
    } else {
      this.selectedImageIndex = this.productImages.length - 1;
    }
  }
}
```

```html
<!-- product-details.component.html -->
<div class="product-details-container">
  <!-- Image Gallery -->
  <div class="image-gallery">
    <!-- Main Image Display -->
    <div class="main-image-container">
      <button class="nav-btn prev" (click)="previousImage()" *ngIf="productImages.length > 1">
        ‹
      </button>
      
      <img 
        [src]="productImages[selectedImageIndex]" 
        [alt]="product?.productName"
        class="main-image">
      
      <button class="nav-btn next" (click)="nextImage()" *ngIf="productImages.length > 1">
        ›
      </button>
      
      <div class="image-counter" *ngIf="productImages.length > 1">
        {{ selectedImageIndex + 1 }} / {{ productImages.length }}
      </div>
    </div>

    <!-- Thumbnail Strip -->
    <div class="thumbnails" *ngIf="productImages.length > 1">
      <div 
        *ngFor="let img of productImages; let i = index"
        class="thumbnail"
        [class.active]="i === selectedImageIndex"
        (click)="selectImage(i)">
        <img [src]="img" [alt]="'Image ' + (i + 1)">
      </div>
    </div>
  </div>

  <!-- Product Info -->
  <div class="product-info">
    <h1>{{ product?.productName }}</h1>
    <p class="price">${{ product?.price }}</p>
    <p class="description">{{ product?.productDescription }}</p>
    <p class="seller">Seller: {{ product?.sellerName }}</p>
    
    <button class="btn-contact" (click)="contactSeller()">
      Contact Seller
    </button>
  </div>
</div>
```

```css
/* product-details.component.css */
.image-gallery {
  margin-bottom: 30px;
}

.main-image-container {
  position: relative;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.main-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
  display: block;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  font-size: 2rem;
  padding: 10px 15px;
  cursor: pointer;
  transition: background 0.3s;
}

.nav-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

.nav-btn.prev {
  left: 10px;
}

.nav-btn.next {
  right: 10px;
}

.image-counter {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.thumbnails {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  overflow-x: auto;
  padding: 10px 0;
}

.thumbnail {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.3s;
}

.thumbnail:hover {
  border-color: #2196F3;
}

.thumbnail.active {
  border-color: #2196F3;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  max-width: 600px;
  margin: 0 auto;
}

.price {
  font-size: 1.8rem;
  color: #2196F3;
  font-weight: bold;
  margin: 10px 0;
}
```

### 7.7 Product Listing with Multiple Images

For product cards in a list view, show the first image:

```html
<!-- product-card.component.html -->
<div class="product-card" (click)="viewDetails(product.productId)">
  <div class="product-image">
    <img [src]="product.productImageUrl || '/assets/no-image.png'" 
         [alt]="product.productName">
    <span class="image-count-badge" *ngIf="product.imageCount > 1">
      📷 {{ product.imageCount }}
    </span>
  </div>
  <div class="product-info">
    <h3>{{ product.productName }}</h3>
    <p class="price">${{ product.price }}</p>
  </div>
</div>
```

**Note**: To show the image count badge, you'll need to either:
1. Fetch image count when loading the product list, or
2. Add an `imageCount` field to the product response (requires backend DTO update)

```html
<!-- product-form.component.html -->
<form [formGroup]="productForm" (ngSubmit)="submitProduct()">
  <input formControlName="productName" placeholder="Product Name">
  <textarea formControlName="description" placeholder="Description"></textarea>
  <input type="number" formControlName="price" placeholder="Price">
  
  <!-- Image Upload -->
  <input type="file" multiple accept="image/jpeg,image/png,image/webp" 
         (change)="onFilesSelected($event)">
  <button type="button" (click)="uploadImages()">Upload Images</button>
  
  <!-- Preview Uploaded Images -->
  <div class="image-preview">
    <div *ngFor="let url of uploadedImageUrls" class="image-item">
      <img [src]="url" alt="Product Image">
      <button type="button" (click)="deleteImage(url)">Delete</button>
    </div>
  </div>
  
  <button type="submit" [disabled]="uploadedImageUrls.length === 0">
    List Product
  </button>
</form>
```

```typescript
// product.service.ts
uploadImages(formData: FormData): Observable<any> {
  return this.http.post(`${this.baseUrl}/users/product/upload-multiple-images`, formData);
}

deleteImage(imageUrl: string): Observable<any> {
  return this.http.delete(`${this.baseUrl}/users/product/delete-image`, {
    params: { imageUrl }
  });
}
```

---

## 8. In-App Messaging

### 8.1 Send Message
**Endpoint**: `POST /messages/send?senderId=123`

**Request**:
```json
{
  "receiverId": 456,
  "productId": 789,
  "content": "Hi! Is this product still available?"
}
```

**Success Response** (200):
```json
{
  "messageId": 1,
  "senderId": 123,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "receiverId": 456,
  "receiverName": "Jane Smith",
  "receiverEmail": "jane@example.com",
  "productId": 789,
  "productTitle": "iPhone 13",
  "content": "Hi! Is this product still available?",
  "sentAt": "2025-11-05T08:00:00",
  "isRead": false,
  "readAt": null
}
```

**Notes**:
- Email notification is automatically sent to the receiver
- Receiver gets email with sender name, product title, and message content

### 8.2 Get Conversations (Inbox)
**Endpoint**: `GET /messages/conversations?userId=123`

**Response** (200):
```json
[
  {
    "otherUserId": 456,
    "otherUserName": "Jane Smith",
    "otherUserEmail": "jane@example.com",
    "productId": 789,
    "productTitle": "iPhone 13",
    "lastMessage": "Yes, it's available!",
    "lastMessageTime": "2025-11-05T08:15:00",
    "hasUnread": true,
    "unreadCount": 2
  }
]
```

### 8.3 Get Conversation Messages
**Endpoint**: `GET /messages/conversation?userId=123&otherUserId=456&productId=789`

**Response** (200):
```json
[
  {
    "messageId": 1,
    "senderId": 123,
    "senderName": "John Doe",
    "receiverId": 456,
    "receiverName": "Jane Smith",
    "productId": 789,
    "productTitle": "iPhone 13",
    "content": "Hi! Is this product still available?",
    "sentAt": "2025-11-05T08:00:00",
    "isRead": true,
    "readAt": "2025-11-05T08:10:00"
  },
  {
    "messageId": 2,
    "senderId": 456,
    "senderName": "Jane Smith",
    "receiverId": 123,
    "receiverName": "John Doe",
    "productId": 789,
    "productTitle": "iPhone 13",
    "content": "Yes, it's available!",
    "sentAt": "2025-11-05T08:15:00",
    "isRead": false,
    "readAt": null
  }
]
```

**Notes**:
- Messages received by `userId` are automatically marked as read when this endpoint is called

### 8.4 Get Unread Count
**Endpoint**: `GET /messages/unread-count?userId=123`

**Response** (200):
```json
5
```

### 8.5 Mark Message as Read
**Endpoint**: `PUT /messages/{messageId}/mark-read?userId=123`

**Response** (200):
```json
"Message marked as read"
```

### Angular Messaging Integration

```typescript
// message.service.ts
export interface SendMessageRequest {
  receiverId: number;
  productId: number;
  content: string;
}

export interface MessageResponse {
  messageId: number;
  senderId: number;
  senderName: string;
  receiverId: number;
  receiverName: string;
  productId: number;
  productTitle: string;
  content: string;
  sentAt: string;
  isRead: boolean;
  readAt?: string;
}

export interface ConversationResponse {
  otherUserId: number;
  otherUserName: string;
  productId: number;
  productTitle: string;
  lastMessage: string;
  lastMessageTime: string;
  hasUnread: boolean;
  unreadCount: number;
}

@Injectable({ providedIn: 'root' })
export class MessageService {
  private baseUrl = 'http://localhost:8080/messages';

  sendMessage(senderId: number, request: SendMessageRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.baseUrl}/send?senderId=${senderId}`, 
      request
    );
  }

  getConversations(userId: number): Observable<ConversationResponse[]> {
    return this.http.get<ConversationResponse[]>(
      `${this.baseUrl}/conversations?userId=${userId}`
    );
  }

  getConversationMessages(
    userId: number, 
    otherUserId: number, 
    productId: number
  ): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(
      `${this.baseUrl}/conversation?userId=${userId}&otherUserId=${otherUserId}&productId=${productId}`
    );
  }

  getUnreadCount(userId: number): Observable<number> {
    return this.http.get<number>(
      `${this.baseUrl}/unread-count?userId=${userId}`
    );
  }

  markAsRead(messageId: number, userId: number): Observable<string> {
    return this.http.put<string>(
      `${this.baseUrl}/${messageId}/mark-read?userId=${userId}`, 
      {}
    );
  }
}
```

```typescript
// inbox.component.ts
export class InboxComponent implements OnInit {
  conversations: ConversationResponse[] = [];
  unreadCount = 0;
  userId: number;

  ngOnInit() {
    this.userId = this.authService.getUserId();
    this.loadConversations();
    this.loadUnreadCount();
    
    // Auto-refresh every 30 seconds
    interval(30000).subscribe(() => {
      this.loadConversations();
      this.loadUnreadCount();
    });
  }

  loadConversations() {
    this.messageService.getConversations(this.userId).subscribe({
      next: (conversations) => {
        this.conversations = conversations;
      }
    });
  }

  loadUnreadCount() {
    this.messageService.getUnreadCount(this.userId).subscribe({
      next: (count) => {
        this.unreadCount = count;
      }
    });
  }

  openConversation(conv: ConversationResponse) {
    this.router.navigate(['/messages/conversation'], {
      queryParams: {
        userId: this.userId,
        otherUserId: conv.otherUserId,
        productId: conv.productId
      }
    });
  }
}
```

```html
<!-- inbox.component.html -->
<div class="inbox">
  <h2>Messages <span class="badge" *ngIf="unreadCount > 0">{{ unreadCount }}</span></h2>
  
  <div *ngFor="let conv of conversations" 
       class="conversation-item" 
       (click)="openConversation(conv)"
       [class.unread]="conv.hasUnread">
    
    <div class="conversation-header">
      <h3>{{ conv.otherUserName }}</h3>
      <span class="badge" *ngIf="conv.hasUnread">{{ conv.unreadCount }}</span>
    </div>
    
    <p class="product-title">{{ conv.productTitle }}</p>
    <p class="last-message">{{ conv.lastMessage }}</p>
    <small class="timestamp">{{ conv.lastMessageTime | date:'short' }}</small>
  </div>
</div>
```

```typescript
// conversation.component.ts
export class ConversationComponent implements OnInit {
  messages: MessageResponse[] = [];
  newMessage = '';
  userId: number;
  otherUserId: number;
  productId: number;

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.userId = +params['userId'];
      this.otherUserId = +params['otherUserId'];
      this.productId = +params['productId'];
      this.loadMessages();
    });
  }

  loadMessages() {
    this.messageService.getConversationMessages(
      this.userId, 
      this.otherUserId, 
      this.productId
    ).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.scrollToBottom();
      }
    });
  }

  sendMessage() {
    const request: SendMessageRequest = {
      receiverId: this.otherUserId,
      productId: this.productId,
      content: this.newMessage
    };

    this.messageService.sendMessage(this.userId, request).subscribe({
      next: (message) => {
        this.messages.push(message);
        this.newMessage = '';
        this.scrollToBottom();
      },
      error: () => {
        this.showError('Failed to send message');
      }
    });
  }

  scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.querySelector('.messages-container');
      chatContainer?.scrollTo(0, chatContainer.scrollHeight);
    }, 100);
  }
}
```

```html
<!-- conversation.component.html -->
<div class="conversation">
  <div class="messages-container">
    <div *ngFor="let msg of messages" 
         class="message"
         [class.sent]="msg.senderId === userId"
         [class.received]="msg.receiverId === userId">
      
      <div class="message-header">
        <strong>{{ msg.senderId === userId ? 'You' : msg.senderName }}</strong>
        <small>{{ msg.sentAt | date:'short' }}</small>
      </div>
      
      <p class="message-content">{{ msg.content }}</p>
      
      <small *ngIf="msg.isRead && msg.senderId === userId" class="read-receipt">
        Read {{ msg.readAt | date:'short' }}
      </small>
    </div>
  </div>
  
  <div class="message-input">
    <input [(ngModel)]="newMessage" 
           placeholder="Type your message..." 
           (keyup.enter)="sendMessage()">
    <button (click)="sendMessage()" [disabled]="!newMessage.trim()">Send</button>
  </div>
</div>
```

```typescript
// product-details.component.ts (Contact Seller Button)
export class ProductDetailsComponent {
  product: MarketPlaceProduct;
  currentUserId: number;

  contactSeller() {
    this.router.navigate(['/messages/conversation'], {
      queryParams: {
        userId: this.currentUserId,
        otherUserId: this.product.sellerId,
        productId: this.product.productId
      }
    });
  }
}
```

```html
<!-- product-details.component.html -->
<button *ngIf="product.sellerId !== currentUserId" 
        (click)="contactSeller()" 
        class="btn-contact">
  Contact Seller
</button>
```

```typescript
// navbar.component.ts (Unread Badge)
export class NavbarComponent implements OnInit {
  unreadCount = 0;

  ngOnInit() {
    const userId = this.authService.getUserId();
    
    // Initial load
    this.messageService.getUnreadCount(userId).subscribe({
      next: (count) => this.unreadCount = count
    });
    
    // Refresh every 30 seconds
    interval(30000).subscribe(() => {
      this.messageService.getUnreadCount(userId).subscribe({
        next: (count) => this.unreadCount = count
      });
    });
  }
}
```

```html
<!-- navbar.component.html -->
<a routerLink="/messages">
  Messages
  <span class="badge" *ngIf="unreadCount > 0">{{ unreadCount }}</span>
</a>
```

---

## 9. Error Handling

### Standard Error Response Format
```json
{
  "timestamp": "2025-11-05T08:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/endpoint"
}
```

### Common HTTP Status Codes
- **200 OK**: Success
- **201 Created**: Resource created successfully
- **204 No Content**: Success with no response body
- **400 Bad Request**: Invalid input data
- **401 Unauthorized**: Invalid credentials
- **403 Forbidden**: Email not verified or insufficient permissions
- **404 Not Found**: Resource doesn't exist
- **409 Conflict**: Duplicate email or resource conflict
- **423 Locked**: Account locked due to failed login attempts
- **500 Internal Server Error**: Server error

### Angular Global Error Handler
```typescript
// error-interceptor.ts
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private toastr: ToastrService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An error occurred';

        if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = error.error.message;
        } else {
          // Server-side error
          switch (error.status) {
            case 400:
              errorMessage = error.error.message || 'Invalid request';
              break;
            case 401:
              errorMessage = 'Invalid credentials';
              break;
            case 403:
              if (!error.error.emailVerified) {
                errorMessage = 'Please verify your email first';
              } else {
                errorMessage = 'Access denied';
              }
              break;
            case 404:
              errorMessage = error.error.message || 'Resource not found';
              break;
            case 409:
              errorMessage = error.error.message || 'Resource already exists';
              break;
            case 423:
              errorMessage = 'Account locked. Check your email for unlock code.';
              break;
            case 500:
              errorMessage = 'Server error. Please try again later.';
              break;
            default:
              errorMessage = error.error.message || 'Unknown error occurred';
          }
        }

        this.toastr.error(errorMessage);
        return throwError(() => error);
      })
    );
  }
}
```

---

## 10. Integration Checklist

### Phase 1: Authentication & Email Verification
- [ ] Update registration form UI
- [ ] Create email verification page with code input
- [ ] Add resend verification button
- [ ] Update login flow to check `emailVerified` flag
- [ ] Handle 403 errors and redirect to verification page
- [ ] Test full registration → verification → login flow

### Phase 2: Two-Factor Authentication
- [ ] Add 2FA setup page with QR code display
- [ ] Store backup codes securely
- [ ] Add 2FA input step to login flow
- [ ] Handle `twoFactorRequired` flag in login response
- [ ] Add 2FA enable/disable toggle in user settings
- [ ] Test login with 2FA enabled and disabled

### Phase 3: Password Management
- [ ] Create forgot password page
- [ ] Create reset password page with code input
- [ ] Create unlock account page
- [ ] Handle 423 account locked errors
- [ ] Test forgot password flow
- [ ] Test account unlock flow

### Phase 4: Role-Based Access Control
- [ ] Create AdminGuard and SuperAdminGuard
- [ ] Apply guards to admin routes
- [ ] Add role-based UI rendering with *ngIf
- [ ] Create admin dashboard components
- [ ] Test navigation restrictions by role
- [ ] Verify SUPER_ADMIN exclusive features

### Phase 5: Image Uploads
- [ ] Add file input to product form
- [ ] Implement image preview functionality
- [ ] Add upload progress indicator
- [ ] Implement image delete functionality
- [ ] Validate file types and sizes on frontend
- [ ] Test upload, preview, and delete flow
- [ ] Handle upload errors gracefully

### Phase 6: In-App Messaging
- [ ] Create inbox component with conversation list
- [ ] Create conversation component with chat interface
- [ ] Add "Contact Seller" button to product details
- [ ] Implement unread count badge in navbar
- [ ] Add auto-refresh for new messages
- [ ] Test sending and receiving messages
- [ ] Verify email notifications
- [ ] Test mark as read functionality

### Phase 7: Testing & Quality Assurance
- [ ] Test all API endpoints in Swagger
- [ ] Verify error handling for all scenarios
- [ ] Test role-based access restrictions
- [ ] Verify email notifications (verification, 2FA, password reset, messages)
- [ ] Test image upload with different file types and sizes
- [ ] Load test messaging with multiple conversations
- [ ] Cross-browser testing
- [ ] Mobile responsiveness testing

### Phase 8: Production Preparation
- [ ] Update base URL to production backend
- [ ] Enable JWT authentication filter on backend
- [ ] Uncomment @EnableMethodSecurity in SecurityConfig
- [ ] Replace MD5 with BCrypt for password hashing
- [ ] Set up HTTPS/SSL
- [ ] Configure CORS for production domain
- [ ] Set up environment-specific configs
- [ ] Create production build

---

## Quick Reference: All Endpoints

### Authentication
```
POST   /auth/register
POST   /auth/verify-email
POST   /auth/resend-verification
POST   /auth/login/v2
POST   /auth/2fa/setup
POST   /auth/2fa/verify
POST   /auth/2fa/disable
POST   /auth/2fa/regenerate-backup-codes
POST   /auth/forgot-password
POST   /auth/reset-password
POST   /auth/unlock-account
```

### Admin (ADMIN, SUPER_ADMIN)
```
GET    /admin/users
GET    /admin/user-profile/by-email?email=...
POST   /admin/create-user
POST   /admin/create-admin          (SUPER_ADMIN only)
DELETE /admin/delete-user/{userId}  (SUPER_ADMIN only)
POST   /admin/ban-user?userId=...
POST   /admin/unban-user?userId=...
POST   /admin/reset-password
```

### Products
```
POST   /users/product
GET    /users/products/available
GET    /users/products/seller/{sellerId}
GET    /users/products/buyer/{buyerId}
PUT    /users/product/{productId}/status
PUT    /users/product/{productId}/sold
PUT    /users/product/{productId}/unavailable
PUT    /users/product/{productId}/price
DELETE /users/product/{productId}
```

### Image Uploads
```
POST   /users/product/upload-image              (single)
POST   /users/product/upload-multiple-images    (multiple)
DELETE /users/product/delete-image?imageUrl=...
```

### Messaging
```
POST   /messages/send?senderId=...
GET    /messages/conversations?userId=...
GET    /messages/conversation?userId=...&otherUserId=...&productId=...
GET    /messages/unread-count?userId=...
PUT    /messages/{messageId}/mark-read?userId=...
```

### User Profile & Reviews
```
GET    /users/profile/{userId}
PUT    /users/profile/{userId}/picture
PUT    /users/profile/{userId}/phone
GET    /users/all/users
GET    /users/seller-info/{sellerId}
POST   /users/reviews/seller
POST   /users/buyer-reviews
GET    /users/reviews/seller/{sellerId}
GET    /users/buyer-reviews/buyer/{buyerId}
GET    /users/product-reviews/{productId}
GET    /users/my-reviews/{userId}
```

---

## Support & Troubleshooting

### Common Issues

**Issue**: 403 Forbidden on all admin endpoints  
**Solution**: Method security is temporarily disabled. For production, uncomment `@EnableMethodSecurity` in SecurityConfig.java and implement JWT authentication filter.

**Issue**: Email not received  
**Solution**: Check `application.properties` for correct SMTP settings. Gmail app password should be 16 characters without spaces.

**Issue**: "No enum constant Role.admin" error  
**Solution**: Run `db-migrations/fix-role-values.sql` to update database role values to uppercase.

**Issue**: Image upload fails  
**Solution**: Verify `file.upload-dir-absolute` exists and has write permissions. Check file size < 5MB and format is JPEG/PNG/WEBP.

**Issue**: Messages not marked as read  
**Solution**: Ensure you're calling `GET /messages/conversation` which auto-marks messages as read for the receiver.

### Backend Configuration Notes
- Port: 8080
- Database: SQL Server on localhost:1433
- SMTP: Gmail (uonmarketplace@gmail.com)
- JWT Secret: Configured in application.properties
- File Upload Dir: `${user.dir}/uploads/products`

---

**Last Updated**: November 5, 2025  
**Questions?** Check Swagger UI at http://localhost:8080/swagger-ui/index.html for live API documentation.
