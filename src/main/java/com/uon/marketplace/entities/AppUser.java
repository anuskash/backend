package com.uon.marketplace.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Role role = Role.USER; // Default to USER role

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "status", nullable = false)
	private String status;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Two-Factor Authentication fields
    @Column(name = "two_factor_enabled", nullable = true, columnDefinition = "BIT DEFAULT 0")
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", nullable = true)
    private String twoFactorSecret; // Encrypted TOTP secret

    @Column(name = "backup_codes", length = 1000, nullable = true)
    private String backupCodes; // Encrypted, comma-separated recovery codes

    @Column(name = "two_factor_verified_at", nullable = true)
    private LocalDateTime twoFactorVerifiedAt; // When 2FA was last verified

    // Email verification fields
    @Column(name = "email_verified", nullable = true, columnDefinition = "BIT DEFAULT 0")
    private Boolean emailVerified = false;

    // Store a hashed version of the verification code for privacy
    @Column(name = "email_verification_code", length = 128, nullable = true)
    private String emailVerificationCode;

    @Column(name = "email_verification_expires_at", nullable = true)
    private LocalDateTime emailVerificationExpiresAt;

    // Email-based 2FA (one-time login code) fields
    @Column(name = "two_factor_email_code", length = 128, nullable = true)
    private String twoFactorEmailCode; // hashed code for login 2FA via email

    @Column(name = "two_factor_email_expires_at", nullable = true)
    private LocalDateTime twoFactorEmailExpiresAt; // expiry for login 2FA code

    // Password reset fields
    @Column(name = "password_reset_token", length = 128, nullable = true)
    private String passwordResetToken; // hashed token for password reset

    @Column(name = "password_reset_expires_at", nullable = true)
    private LocalDateTime passwordResetExpiresAt; // expiry for reset token

    // Failed login tracking
    @Column(name = "failed_login_attempts", nullable = true, columnDefinition = "INT DEFAULT 0")
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until", nullable = true)
    private LocalDateTime accountLockedUntil; // when account lock expires

    @Column(name = "unlock_code", length = 128, nullable = true)
    private String unlockCode; // hashed code to unlock account

    @Column(name = "unlock_code_expires_at", nullable = true)
    private LocalDateTime unlockCodeExpiresAt; // expiry for unlock code
}
