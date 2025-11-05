package com.uon.marketplace.services;

import com.uon.marketplace.dto.requests.TwoFactorLoginRequest;
import com.uon.marketplace.dto.requests.TwoFactorVerifyRequest;
import com.uon.marketplace.dto.responses.LoginResponse;
import com.uon.marketplace.dto.responses.TwoFactorSetupResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.repositories.AppUserRepository;
import com.uon.marketplace.utils.PasswordHashUtil;
import com.uon.marketplace.services.EmailVerificationService;
import com.uon.marketplace.services.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced Authentication Service with Two-Factor Authentication
 * Implements secure login flow with TOTP-based 2FA
 */
@Service
public class AuthenticationService {

    private final AppUserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService; // reuse code gen + hashing
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    public AuthenticationService(
            AppUserRepository userRepository,
            TwoFactorAuthService twoFactorAuthService,
            JwtService jwtService,
            EmailVerificationService emailVerificationService,
            EmailService emailService,
            PasswordResetService passwordResetService
    ) {
        this.userRepository = userRepository;
        this.twoFactorAuthService = twoFactorAuthService;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Enhanced login with 2FA support
     * Flow:
     * 1. Validate credentials
     * 2. If 2FA enabled, require code
     * 3. Generate JWT token only after full verification
     */
    @Transactional
    public LoginResponse login(TwoFactorLoginRequest request) {
        // Find user by email
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return new LoginResponse(null, null, null, null, false,
                    "Invalid email or password", false);
        }

        // Verify password
        String hashedPassword = PasswordHashUtil.hashWithMD5(request.getPassword());
        if (!user.getPasswordHash().equals(hashedPassword)) {
            // Record failed login
            passwordResetService.recordFailedLogin(user);
            return new LoginResponse(null, null, null, null, false,
                    "Invalid email or password", false);
        }

        // Check if account is locked
        if (passwordResetService.isAccountLocked(user)) {
            return new LoginResponse(null, null, null, null, false,
                    "Account is locked due to failed login attempts. Check your email for unlock code.", false);
        }

        // Check account status
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            return new LoginResponse(null, null, null, null, false,
                    "Account is not active", false);
        }

        // Record successful login (reset failed attempts)
        passwordResetService.recordSuccessfulLogin(user);

        // Check if 2FA is enabled
        if (user.getTwoFactorEnabled()) {
            // If TOTP secret exists, use TOTP/backup code path
            if (user.getTwoFactorSecret() != null && !user.getTwoFactorSecret().isEmpty()) {
                if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                    return new LoginResponse(
                            user.getUserId(), user.getEmail(), user.getRole().name(), null, true,
                            "Two-factor authentication required (authenticator app)", true);
                }

                boolean codeValid = false;
                if (twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), request.getTwoFactorCode())) {
                    codeValid = true;
                    user.setTwoFactorVerifiedAt(LocalDateTime.now());
                } else {
                    String remainingCodes = twoFactorAuthService.verifyAndRemoveBackupCode(
                            user.getBackupCodes(), request.getTwoFactorCode());
                    if (remainingCodes != null) {
                        codeValid = true;
                        user.setBackupCodes(remainingCodes);
                        user.setTwoFactorVerifiedAt(LocalDateTime.now());
                        userRepository.save(user);
                    }
                }

                if (!codeValid) {
                    return new LoginResponse(null, null, null, null, true,
                            "Invalid two-factor code", false);
                }

                String token = jwtService.generateToken(
                        user.getUserId(), user.getEmail(), user.getRole().name(), true);
                return new LoginResponse(user.getUserId(), user.getEmail(), user.getRole().name(), token, false,
                        "Login successful with 2FA", true);
            }

            // Otherwise, use email-based one-time code 2FA
            // Step 1: No code provided -> generate, store, and email it
            if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                String code = emailVerificationService.generateCode();
                String hashed = emailVerificationService.hashCode(code);
                user.setTwoFactorEmailCode(hashed);
                user.setTwoFactorEmailExpiresAt(LocalDateTime.now().plusMinutes(10));
                userRepository.save(user);

                String subject = "Your UON Marketplace login code";
                String body = "Hi,\n\n" +
                        "Use this verification code to finish signing in: " + code + "\n\n" +
                        "This code expires in 10 minutes. If you didn't try to sign in, you can ignore this email.\n\n" +
                        "— UON Marketplace";
                emailService.send(user.getEmail(), subject, body);

                return new LoginResponse(
                        user.getUserId(), user.getEmail(), user.getRole().name(), null, true,
                        "We sent a 6-digit code to your email. Enter it to continue.", true);
            }

            // Step 2: Verify provided email code
            if (user.getTwoFactorEmailCode() == null || user.getTwoFactorEmailExpiresAt() == null) {
                return new LoginResponse(null, null, null, null, true,
                        "No active verification code. Please request a new login code.", false);
            }
            if (user.getTwoFactorEmailExpiresAt().isBefore(LocalDateTime.now())) {
                return new LoginResponse(null, null, null, null, true,
                        "Verification code expired. Please request a new login code.", false);
            }

            String providedHashed = emailVerificationService.hashCode(request.getTwoFactorCode());
            if (!providedHashed.equals(user.getTwoFactorEmailCode())) {
                return new LoginResponse(null, null, null, null, true,
                        "Invalid verification code.", false);
            }

            // Clear used code and issue JWT
            user.setTwoFactorEmailCode(null);
            user.setTwoFactorEmailExpiresAt(null);
            user.setTwoFactorVerifiedAt(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getRole().name(), true);
            return new LoginResponse(user.getUserId(), user.getEmail(), user.getRole().name(), token, false,
                    "Login successful with email verification", true);
        } else {
            // No 2FA, generate token directly
            String token = jwtService.generateToken(
                    user.getUserId(),
                    user.getEmail(),
                    user.getRole().name(),
                    false
            );

            return new LoginResponse(
                    user.getUserId(),
                    user.getEmail(),
                    user.getRole().name(),
                    token,
                    false,
                    "Login successful",
                    true
            );
        }
    }

    /**
     * Initialize 2FA setup for user
     * Generates secret, QR code, and backup codes
     */
    @Transactional
    public TwoFactorSetupResponse setupTwoFactor(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Account not active. Please verify your email first.");
        }

        if (user.getTwoFactorEnabled()) {
            throw new RuntimeException("Two-factor authentication is already enabled");
        }

        // Generate new secret
        String secret = twoFactorAuthService.generateSecret();
        
        // Generate QR code
        String qrCodeUrl = twoFactorAuthService.generateQRCodeUrl(user.getEmail(), secret);
        
        // Generate manual entry key
        String manualEntryKey = twoFactorAuthService.getManualEntryKey(secret);
        
        // Generate backup codes
        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();

        // Store secret and backup codes (NOT enabled yet, user must verify first)
        user.setTwoFactorSecret(secret);
        user.setBackupCodes(twoFactorAuthService.formatBackupCodesForStorage(backupCodes));
        user.setTwoFactorEnabled(false); // Will be enabled after verification
        userRepository.save(user);

        return new TwoFactorSetupResponse(
                secret,
                qrCodeUrl,
                manualEntryKey,
                "UON Marketplace",
                user.getEmail(),
                backupCodes
        );
    }

    /**
     * Verify and enable 2FA
     * User must provide valid TOTP code to confirm setup
     */
    @Transactional
    public boolean verifyAndEnableTwoFactor(TwoFactorVerifyRequest request) {
        AppUser user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("Two-factor setup not initiated");
        }

        // Verify the code
        boolean valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), request.getCode());
        
        if (valid) {
            user.setTwoFactorEnabled(true);
            user.setTwoFactorVerifiedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Disable 2FA for user
     * Requires password verification for security
     */
    @Transactional
    public boolean disableTwoFactor(Long userId, String password) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify password
        String hashedPassword = PasswordHashUtil.hashWithMD5(password);
        if (!user.getPasswordHash().equals(hashedPassword)) {
            throw new RuntimeException("Invalid password");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setBackupCodes(null);
        user.setTwoFactorVerifiedAt(null);
        userRepository.save(user);

        return true;
    }

    /**
     * Regenerate backup codes
     * Requires password or 2FA code for security
     */
    @Transactional
    public List<String> regenerateBackupCodes(Long userId, String verificationCode) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getTwoFactorEnabled()) {
            throw new RuntimeException("Two-factor authentication is not enabled");
        }

        // Verify current TOTP code
        boolean valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), verificationCode);
        if (!valid) {
            throw new RuntimeException("Invalid verification code");
        }

        // Generate new backup codes
        List<String> newBackupCodes = twoFactorAuthService.generateBackupCodes();
        user.setBackupCodes(twoFactorAuthService.formatBackupCodesForStorage(newBackupCodes));
        userRepository.save(user);

        return newBackupCodes;
    }

    /**
     * Check if user has 2FA enabled
     */
    public boolean isTwoFactorEnabled(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getTwoFactorEnabled();
    }

    /**
     * Helper: fetch user by email or throw.
     */
    public AppUser getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
