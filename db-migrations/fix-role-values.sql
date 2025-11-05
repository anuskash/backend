-- Fix role values: update lowercase to uppercase to match Role enum constants
-- Run this script to fix existing data in your database

-- Update all lowercase 'user' to 'USER'
UPDATE users 
SET role = 'USER' 
WHERE role = 'user';

-- Update all lowercase 'admin' to 'ADMIN'
UPDATE users 
SET role = 'ADMIN' 
WHERE role = 'admin';

-- Update all lowercase 'super_admin' to 'SUPER_ADMIN'
UPDATE users 
SET role = 'SUPER_ADMIN' 
WHERE role = 'super_admin';

-- Verify the update
SELECT role, COUNT(*) as count 
FROM users 
GROUP BY role;
