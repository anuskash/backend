-- Migration: Add Role enum support to app_user table
-- Date: 2025-11-05
-- Description: Update role column to support Role enum (USER, ADMIN, SUPER_ADMIN)

-- Step 1: Update existing users with 'User' to 'USER' (standardize to enum format)
UPDATE users 
SET role = 'USER' 
WHERE role = 'User' OR role = 'user';

-- Step 2: Update existing admins if any
UPDATE users 
SET role = 'ADMIN' 
WHERE role = 'Admin' OR role = 'admin';

-- Step 3: Update super admins if any
UPDATE users 
SET role = 'SUPER_ADMIN' 
WHERE role = 'Super Admin' OR role = 'super_admin' OR role = 'SuperAdmin';

-- Step 4: Set default to USER for any null values
UPDATE users 
SET role = 'USER' 
WHERE role IS NULL;

-- Step 5: Ensure column has proper constraints
-- (Hibernate will handle this with @Enumerated, but for manual DB setup:)
-- ALTER TABLE users ALTER COLUMN role VARCHAR(20) NOT NULL;

-- Note: Run this script if Hibernate auto-update doesn't handle existing data properly
