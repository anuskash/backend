# Role-Based Access Control (RBAC) Implementation

## Overview
This document describes the role-based access control system implemented for the UON Marketplace backend, including email verification, 2FA authentication, and account security features.

## Authentication & Verification Flow

### Registration Process
1. **User registers** via `/auth/register` → Gets `USER` role
2. **Email verification code sent** → User receives 6-digit code in email
3. **User verifies email** via `/auth/verify-email` → Account becomes `email_verified: true`
4. **User can now login** with email + password
5. **2FA setup prompt** → User encouraged to enable TOTP for enhanced security

### Login Security Layers
1. **Standard login** → Email + password (if 2FA disabled)
2. **2FA login** → Email + password + TOTP code (if 2FA enabled)
3. **Failed login protection**:
   - After **3 failed attempts** → Verification code sent to email
   - User must enter verification code before trying again
   - Options presented: Reset password OR Login with 2FA

### Password Reset Flow
**Initiated from login screen (Forgot Password):**
1. User clicks "Forgot Password"
2. System asks: **"Use 2FA code OR Reset via email link?"**
   - Option A: Enter 2FA code → Login directly
   - Option B: Email reset link → User clicks link → Set new password

**Initiated from user settings (Change Password):**
1. User requests password change
2. **Email reset link sent** → User clicks link → Set new password
3. Confirmation email sent after successful reset

### Admin Verification vs Email Verification
**Email Verification** (Automatic, Required):
- Sent at registration
- User verifies their email address
- Required to login

**Admin Verification** (Manual, Optional):
- Admin can mark users as "verified" via `/admin/verify-user/{userId}`
- Visual badge/status indicator
- Indicates trusted seller/buyer (e.g., for business accounts)
- **Does NOT affect login ability**

## Security Features

### 1. Email Verification (Required for All Users)
**Endpoints:**
- `POST /auth/register` → Sends verification code
- `POST /auth/verify-email` → User enters code from email
- `POST /auth/resend-verification` → Resend code if expired

**Fields in `users` table:**
- `email_verified` → `true` after successful verification
- `email_verification_code` → 6-digit code
- `email_verification_expires_at` → Code valid for 10 minutes

**Logic:**
- Registration → Code sent → User cannot login until verified
- Verification code expires after 10 minutes
- User can request new code via resend endpoint

### 2. Two-Factor Authentication (Optional, Recommended)
**Endpoints:**
- `POST /auth/setup-2fa` → Generate QR code (Google Authenticator)
- `POST /auth/verify-2fa` → Verify TOTP code to enable
- `POST /auth/disable-2fa` → Disable 2FA (requires current TOTP code)
- `POST /auth/login-2fa` → Login with email + password + TOTP

**Fields in `users` table:**
- `two_factor_enabled` → `true` when active
- `two_factor_secret` → TOTP secret key
- `two_factor_verified_at` → When user completed setup
- `backup_codes` → Recovery codes (comma-separated)

**Logic:**
- User sets up 2FA after first login (prompted by frontend)
- QR code scanned with authenticator app
- User verifies with 6-digit TOTP code
- Backup codes generated for account recovery

### 3. Account Lockout (Failed Login Protection)
**Endpoints:**
- `POST /auth/unlock-account` → User enters unlock code sent to email
- `POST /auth/send-unlock-code` → Request new unlock code

**Fields in `users` table:**
- `failed_login_attempts` → Counter (resets to 0 on success)
- `account_locked_until` → Timestamp when lock expires
- `unlock_code` → 6-digit code sent via email
- `unlock_code_expires_at` → Code valid for 15 minutes

**Logic:**
- **3 failed login attempts** → Account locked for 30 minutes
- Email sent with unlock code
- User options:
  1. Enter unlock code → Can try login again
  2. Request password reset → Set new password
  3. Use 2FA code (if enabled) → Bypass lockout

### 4. Password Reset
**Endpoints:**
- `POST /auth/forgot-password` → Request reset link
- `POST /auth/reset-password` → Set new password with token
- `POST /auth/change-password` → Change password from settings (logged in)

**Fields in `users` table:**
- `password_reset_token` → UUID token in email link
- `password_reset_expires_at` → Token valid for 1 hour

**Logic:**
- User clicks "Forgot Password" → Email sent with reset link
- Link format: `http://marketplace.com/reset-password?token=UUID`
- User clicks link → Frontend shows "Set New Password" form
- User submits new password with token → Password updated
- Token invalidated after use or expiration

### 5. Admin User Verification (Trust Badge)
**Endpoint:**
- `PUT /admin/verify-user/{userId}` → Admin marks user as verified

**Purpose:**
- Visual trust indicator (verified seller/buyer badge)
- Separate from email verification
- Used for business accounts, trusted sellers, etc.
- **Does not affect authentication** - purely for reputation

## Role Hierarchy

### 1. **USER** (Default Role)
- Assigned to all new registrations via `/auth/register`
- **Must verify email before first login**
- **Permissions:**
  - Manage own profile (`/users/profile/**`)
  - Create, update, delete own products (`/users/product/**`)
  - Upload product images (`/users/product/upload-image`, `/users/product/upload-images`)
  - Write reviews for sellers and buyers (`/users/seller-review`, `/users/buyer-review`)
  - View own reviews and ratings (`/users/my-reviews`, `/users/average-rating`)
  - Send/receive messages about products

### 2. **ADMIN**
- Created by SUPER_ADMIN via `/admin/create-admin`
- **Permissions:** All USER permissions PLUS:
  - View all users (`GET /admin/users`)
  - Create regular users (`POST /admin/create-user`)
  - View any user's profile (`GET /admin/user-profile/{userId}` or `/admin/user-profile/by-email`)
  - View any user's reviews (`GET /admin/buyer-reviews/{userId}`, `/admin/seller-reviews/{userId}`)
  - Ban/unban users (`POST /admin/ban-user/{userId}`, `/admin/unban-user/{userId}`)
  - Verify users (`PUT /admin/verify-user/{userId}`)
  - Force reset user passwords (`POST /admin/reset-password`)

### 3. **SUPER_ADMIN**
- Must be created manually in database or via initial seed script
- **Permissions:** All ADMIN permissions PLUS:
  - Create ADMIN users (`POST /admin/create-admin`) ⭐ SUPER_ADMIN only
  - Permanently delete users (`DELETE /admin/delete-user/{userId}`) ⭐ SUPER_ADMIN only

## Technical Implementation

### Entity Changes
**File:** `src/main/java/com/uon/marketplace/entities/Role.java`
```java
public enum Role {
    USER,           // Regular users
    ADMIN,          // Administrators
    SUPER_ADMIN     // Super administrators
}
```

**File:** `src/main/java/com/uon/marketplace/entities/AppUser.java`
```java
@Enumerated(EnumType.STRING)
@Column(name = "role", nullable = false)
private Role role = Role.USER; // Default to USER role
```

### Security Configuration
**File:** `src/main/java/com/uon/marketplace/config/SecurityConfig.java`
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize
public class SecurityConfig { ... }
```

### Controller Protection
**File:** `src/main/java/com/uon/marketplace/controllers/AdminController.java`

**Class-level protection** (all admin endpoints require ADMIN or SUPER_ADMIN):
```java
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController { ... }
```

**Method-level protection** (specific endpoints require SUPER_ADMIN):
```java
@PreAuthorize("hasRole('SUPER_ADMIN')")
@PostMapping("/create-admin")
public ResponseEntity<AppUserResponse> createAdmin(...) { ... }

@PreAuthorize("hasRole('SUPER_ADMIN')")
@DeleteMapping("/delete-user/{userId}")
public ResponseEntity<String> deleteUser(...) { ... }
```

## Database Migration

**File:** `db-migrations/add-role-enum.sql`

The migration script updates existing users to use the new enum format:
- `'User'` → `'USER'`
- `'Admin'` → `'ADMIN'`
- `'Super Admin'` → `'SUPER_ADMIN'`

Since `spring.jpa.hibernate.ddl-auto=update` is enabled, Hibernate will automatically handle schema updates. Run the migration script manually if needed:

```sql
UPDATE users SET role = 'USER' WHERE role = 'User' OR role = 'user';
UPDATE users SET role = 'ADMIN' WHERE role = 'Admin' OR role = 'admin';
UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'Super Admin' OR role = 'super_admin';
UPDATE users SET role = 'USER' WHERE role IS NULL;
```

## Service-Level Role Assignment

### Registration (Public)
**File:** `src/main/java/com/uon/marketplace/services/LoginService.java`
```java
// All new registrations get USER role
appUser.setRole(Role.USER);
```

### Admin Operations
**File:** `src/main/java/com/uon/marketplace/services/AdminService.java`

**Creating regular users:**
```java
public AppUserResponse createUser(CreateUserRequest request) {
    appUser.setRole(Role.USER); // Regular user creation
    ...
}
```

**Creating admins:**
```java
public AppUserResponse createAdmin(CreateUserRequest request) {
    appUser.setRole(Role.ADMIN); // Admin user creation
    ...
}
```

## Important Notes

### ⚠️ JWT Filter Required for Production
**Current State:** All endpoints use `.permitAll()` in `SecurityConfig.java`

The `@PreAuthorize` annotations are configured but **NOT ENFORCED** until a JWT authentication filter is implemented. This is intentional for development/testing.

**For production deployment:**
1. Implement `JwtAuthenticationFilter` to extract and validate JWT tokens
2. Set `Authentication` in `SecurityContext` with user ID and role
3. Update `SecurityConfig.securityFilterChain()` to replace `.permitAll()` with `.authenticated()`
4. Ensure login response includes JWT token with role embedded

### Creating the First SUPER_ADMIN

Since registration defaults to USER and creating admins requires SUPER_ADMIN access, you need to create the first super admin manually:

**Option 1: Direct database insert**
```sql
INSERT INTO users (role, email, password_hash, status, created_at, email_verified, two_factor_enabled)
VALUES ('SUPER_ADMIN', 'superadmin@marketplace.com', '<hashed_password>', 'active', GETDATE(), 1, 0);
```

**Option 2: Update existing user**
```sql
UPDATE users 
SET role = 'SUPER_ADMIN' 
WHERE email = 'your-existing-admin@example.com';
```

**Option 3: Temporary bypass endpoint (remove after setup)**
Create a one-time setup endpoint that creates a super admin (secure with strong temporary password).

## Testing RBAC

### Without JWT Filter (Current State)
Role-based access is **not enforced** - you can test endpoint logic but all requests will succeed.

### With JWT Filter (Production)
1. Register a user → gets `USER` role → token with role: "USER"
2. Login → JWT token contains role claim
3. Try accessing `/admin/users` → ❌ 403 Forbidden
4. Manually promote to SUPER_ADMIN in database
5. Login again → JWT token contains role: "SUPER_ADMIN"
6. Try accessing `/admin/create-admin` → ✅ 200 OK

## API Response Changes

### Login Response
The `role` field now returns enum values as strings:
```json
{
  "userId": 123,
  "email": "user@example.com",
  "role": "USER",  // or "ADMIN" or "SUPER_ADMIN"
  "token": "eyJhbGc...",
  "requires2FA": false
}
```

### Admin User Profile
```json
{
  "user": {
    "userId": 456,
    "email": "admin@example.com",
    "role": "ADMIN",
    "status": "active",
    "emailVerified": true,
    "twoFactorEnabled": false
  },
  ...
}
```

## Complete User Journey

### New User Registration & Login
```
1. POST /auth/register
   ↓ (email with 6-digit code sent)
2. POST /auth/verify-email (enter code)
   ↓ (email_verified = true)
3. POST /auth/login (email + password)
   ↓ (successful login)
4. Frontend prompts: "Enable 2FA for extra security?"
   ↓ (user clicks "Set up")
5. POST /auth/setup-2fa (get QR code)
   ↓ (scan with Google Authenticator)
6. POST /auth/verify-2fa (enter TOTP code)
   ✅ 2FA enabled! (backup codes saved)
```

### Forgot Password Flow
```
User on login screen → Clicks "Forgot Password"
   ↓
Frontend shows: "How do you want to recover?"
   ├─ Option 1: "Use 2FA Code"
   │    ↓
   │  POST /auth/login-2fa (email + TOTP)
   │    ✅ Logged in
   │
   └─ Option 2: "Email Reset Link"
        ↓
      POST /auth/forgot-password (email)
        ↓ (email sent with token)
      User clicks link → Frontend shows password form
        ↓
      POST /auth/reset-password (token + new password)
        ✅ Password reset
```

### Failed Login Flow
```
1. POST /auth/login (wrong password)
   ↓ failed_login_attempts = 1
2. POST /auth/login (wrong password again)
   ↓ failed_login_attempts = 2
3. POST /auth/login (wrong password third time)
   ↓ Account locked! (unlock code sent to email)
   
Frontend shows: "Account temporarily locked. Check your email."
   ├─ Option 1: Enter unlock code
   │    ↓
   │  POST /auth/unlock-account (code)
   │    ✅ Can try login again
   │
   ├─ Option 2: Reset password
   │    ↓
   │  POST /auth/forgot-password
   │    ✅ Set new password
   │
   └─ Option 3: Use 2FA (if enabled)
        ↓
      POST /auth/login-2fa (email + TOTP)
        ✅ Logged in (bypass lockout)
```

### Change Password from Settings
```
User logged in → Goes to Settings → "Change Password"
   ↓
Frontend: "We'll send you a secure link"
   ↓
POST /auth/change-password (userId)
   ↓ (email sent with reset link)
User clicks link → Sets new password
   ↓
POST /auth/reset-password (token + new password)
   ✅ Password changed (confirmation email sent)
```

## Security Best Practices

1. **Never hardcode super admin credentials** - use environment variables
2. **Audit super admin actions** - log all create-admin and delete-user operations
3. **Limit super admin accounts** - only create as many as absolutely necessary
4. **Rotate super admin passwords** - enforce strong password policy
5. **Enable 2FA for admins** - require TOTP for all admin accounts
6. **Monitor failed admin logins** - alert on suspicious activity

## Future Enhancements

- [ ] Implement JWT authentication filter
- [ ] Add audit logging for admin actions
- [ ] Create role promotion/demotion endpoints for SUPER_ADMIN
- [ ] Add granular permissions (beyond just roles)
- [ ] Implement IP whitelisting for admin endpoints
- [ ] Add rate limiting for admin operations
- [ ] Create admin activity dashboard
