package com.uon.marketplace.services;

import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.repositories.AppUserRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class EmailVerificationService {

    private static final int CODE_TTL_MINUTES = 15; // 15 minutes
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(AppUserRepository appUserRepository, EmailService emailService) {
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
    }

    // Generate a 6-digit numeric code as a string
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
            throw new RuntimeException("Failed to hash verification code", e);
        }
    }

    public void createAndSendVerificationCode(AppUser user) {
        String code = generateCode();
        user.setEmailVerificationCode(hashCode(code));
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
        appUserRepository.save(user);

        String subject = "Verify your UON Marketplace account";
        String body = "Hi,\n\n" +
                "Thanks for registering with UON Marketplace. Please verify your email using this code:\n\n" +
                code + "\n\n" +
                "This code expires in " + CODE_TTL_MINUTES + " minutes.\n\n" +
                "If you didn't request this, you can ignore this email.\n\n" +
                "— UON Marketplace";
        emailService.send(user.getEmail(), subject, body);
    }

    public boolean verifyCodeForUser(String email, String providedCode) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmailVerified() != null && user.getEmailVerified()) {
            return true; // already verified
        }
        if (user.getEmailVerificationCode() == null || user.getEmailVerificationExpiresAt() == null) {
            throw new RuntimeException("No verification in progress");
        }
        if (user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code expired");
        }

        String hashed = hashCode(providedCode);
        if (!hashed.equals(user.getEmailVerificationCode())) {
            return false;
        }

        // Mark as verified
        user.setEmailVerified(true);
        user.setStatus("active");
        user.setEmailVerificationCode(null);
        user.setEmailVerificationExpiresAt(null);
        appUserRepository.save(user);
        return true;
    }

    public void resendIfPending(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getEmailVerified() != null && user.getEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }
        createAndSendVerificationCode(user);
    }
}
