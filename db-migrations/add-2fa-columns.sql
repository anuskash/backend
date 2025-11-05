-- Two-Factor Authentication Schema Migration
-- Run this script manually or restart the app to let Hibernate auto-create

-- Add 2FA columns to users table
ALTER TABLE users 
ADD two_factor_enabled BIT NOT NULL DEFAULT 0;

ALTER TABLE users 
ADD two_factor_secret NVARCHAR(255) NULL;

ALTER TABLE users 
ADD backup_codes NVARCHAR(1000) NULL;

ALTER TABLE users 
ADD two_factor_verified_at DATETIME2 NULL;

-- Update existing users to have 2FA disabled by default
UPDATE users 
SET two_factor_enabled = 0 
WHERE two_factor_enabled IS NULL;

-- Verify columns were added
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users' 
AND COLUMN_NAME IN ('two_factor_enabled', 'two_factor_secret', 'backup_codes', 'two_factor_verified_at')
ORDER BY ORDINAL_POSITION;
