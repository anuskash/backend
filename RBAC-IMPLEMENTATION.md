# Role-Based Access Control (RBAC) Implementation

## Overview
This document describes the role-based access control system implemented for the UON Marketplace backend.

## Role Hierarchy

### 1. **USER** (Default Role)
- Assigned to all new registrations via `/auth/register`
- **Permissions:**
  - Manage own profile (`/users/profile/**`)
  - Create, update, delete own products (`/users/product/**`)
  - Upload product images (`/users/product/upload-image`, `/users/product/upload-images`)
  - Write reviews for sellers and buyers (`/users/seller-review`, `/users/buyer-review`)
  - View own reviews and ratings (`/users/my-reviews`, `/users/average-rating`)

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
