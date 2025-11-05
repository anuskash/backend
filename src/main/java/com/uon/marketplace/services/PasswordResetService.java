package com.uon.marketplace.services;

import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.repositories.AppUserRepository;
import com.uon.marketplace.utils.PasswordHashUtil;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final int CODE_TTL_MINUTES = 15; // 15 minutes
    private static final int UNLOCK_CODE_TTL_MINUTES = 30; // 30 minutes for unlock
    private static final int MAX_FAILED_ATTEMPTS = 2; // Lock after 2 failed attempts
    private static final int LOCK_DURATION_MINUTES = 30; // Lock for 30 minutes

    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(AppUserRepository appUserRepository, EmailService emailService) {
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
    }

    // Generate a 6-digit numeric code
    public String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000); // 100000-999999
        return String.valueOf(code);
    }

    // Hash code for storage (privacy)
    public String hashCode(String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(code.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash code", e);
        }
    }

    /**
     * Initiate password reset by sending a code to the user's email
     */
    public void sendPasswordResetCode(String email) {
        AppUser user = appUserRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code = generateCode();
        user.setPasswordResetToken(hashCode(code));
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
        appUserRepository.save(user);

        String subject = "Reset your UON Marketplace password";
        String body = "Hi,\n\n" +
                "You requested to reset your password. Use this code:\n\n" +
                code + "\n\n" +
                "This code expires in " + CODE_TTL_MINUTES + " minutes.\n\n" +
                "If you didn't request this, you can ignore this email.\n\n" +
                "— UON Marketplace";
        emailService.send(user.getEmail(), subject, body);
    }

    /**
     * Reset password using the code
     */
    public boolean resetPassword(String email, String providedCode, String newPassword) {
        AppUser user = appUserRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPasswordResetToken() == null || user.getPasswordResetExpiresAt() == null) {
            throw new RuntimeException("No password reset in progress");
        }
        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset code expired");
        }

        String hashed = hashCode(providedCode);
        if (!hashed.equals(user.getPasswordResetToken())) {
            return false;
        }

        // Reset password and clear reset fields
        user.setPasswordHash(PasswordHashUtil.hashWithMD5(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        user.setFailedLoginAttempts(0); // Reset failed attempts on successful password reset
        user.setAccountLockedUntil(null);
        appUserRepository.save(user);
        return true;
    }

    /**
     * Track failed login and lock account if necessary
     */
    public void recordFailedLogin(AppUser user) {
        Integer attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        attempts++;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            // Lock account and send unlock code
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            
            String unlockCode = generateCode();
            user.setUnlockCode(hashCode(unlockCode));
            user.setUnlockCodeExpiresAt(LocalDateTime.now().plusMinutes(UNLOCK_CODE_TTL_MINUTES));
            
            appUserRepository.save(user);

            String subject = "Your UON Marketplace account has been locked";
            String body = "Hi,\n\n" +
                    "Your account has been locked due to " + MAX_FAILED_ATTEMPTS + " failed login attempts.\n\n" +
                    "Use this code to unlock your account:\n\n" +
                    unlockCode + "\n\n" +
                    "This code expires in " + UNLOCK_CODE_TTL_MINUTES + " minutes.\n\n" +
                    "If you didn't try to log in, please reset your password immediately.\n\n" +
                    "— UON Marketplace";
            emailService.send(user.getEmail(), subject, body);
        } else {
            appUserRepository.save(user);
        }
    }

    /**
     * Reset failed login counter on successful login
     */
    public void recordSuccessfulLogin(AppUser user) {
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            user.setUnlockCode(null);
            user.setUnlockCodeExpiresAt(null);
            appUserRepository.save(user);
        }
    }

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked(AppUser user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }
        if (user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
            // Lock expired, clear it
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);
            user.setUnlockCode(null);
            user.setUnlockCodeExpiresAt(null);
            appUserRepository.save(user);
            return false;
        }
        return true;
    }

    /**
     * Unlock account using the code
     */
    public boolean unlockAccount(String email, String providedCode) {
        AppUser user = appUserRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUnlockCode() == null || user.getUnlockCodeExpiresAt() == null) {
            throw new RuntimeException("No unlock code available");
        }
        if (user.getUnlockCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Unlock code expired");
        }

        String hashed = hashCode(providedCode);
        if (!hashed.equals(user.getUnlockCode())) {
            return false;
        }

        // Unlock account and clear unlock fields
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        user.setUnlockCode(null);
        user.setUnlockCodeExpiresAt(null);
        appUserRepository.save(user);
        return true;
    }
}
