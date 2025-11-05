package com.uon.marketplace.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Two-Factor Authentication Service
 * Implements TOTP (RFC 6238) using Google Authenticator compatible algorithm
 * Privacy-safe: secrets are never logged or exposed after initial setup
 */
@Service
public class TwoFactorAuthService {

    private final GoogleAuthenticator googleAuthenticator;
    private static final String ISSUER = "UON Marketplace";
    private static final int BACKUP_CODES_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;

    public TwoFactorAuthService() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    /**
     * Generate a new TOTP secret for user
     * @return base32 encoded secret
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    /**
     * Generate QR code URL for authenticator app
     * @param email user email (account identifier)
     * @param secret TOTP secret
     * @return data URL containing QR code image (base64 encoded PNG)
     */
    public String generateQRCodeUrl(String email, String secret) {
        try {
            // Generate the OTP auth URL
            String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                ISSUER,
                email,
                new GoogleAuthenticatorKey.Builder(secret).build()
            );

            // Generate QR code image
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 300, 300);
            
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Convert to base64 data URL
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Get manual entry key (formatted secret for manual entry)
     * @param secret TOTP secret
     * @return formatted secret (e.g., "XXXX XXXX XXXX XXXX")
     */
    public String getManualEntryKey(String secret) {
        // Format secret in groups of 4 for easier manual entry
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < secret.length(); i += 4) {
            if (i > 0) formatted.append(" ");
            formatted.append(secret.substring(i, Math.min(i + 4, secret.length())));
        }
        return formatted.toString();
    }

    /**
     * Verify TOTP code
     * @param secret user's TOTP secret
     * @param code 6-digit code from authenticator app
     * @return true if code is valid
     */
    public boolean verifyCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Generate backup recovery codes
     * @return list of secure random backup codes
     */
    public List<String> generateBackupCodes() {
        SecureRandom secureRandom = new SecureRandom();
        return IntStream.range(0, BACKUP_CODES_COUNT)
                .mapToObj(i -> generateRandomCode(secureRandom))
                .collect(Collectors.toList());
    }

    /**
     * Verify backup code
     * @param storedCodes comma-separated list of valid backup codes
     * @param providedCode code provided by user
     * @return updated list of remaining codes (with used code removed), or null if invalid
     */
    public String verifyAndRemoveBackupCode(String storedCodes, String providedCode) {
        if (storedCodes == null || storedCodes.isEmpty()) {
            return null;
        }

        String[] codes = storedCodes.split(",");
        StringBuilder remaining = new StringBuilder();
        boolean found = false;

        for (String code : codes) {
            if (code.trim().equals(providedCode.trim())) {
                found = true;
                // Don't add this code to remaining list (it's consumed)
            } else {
                if (remaining.length() > 0) remaining.append(",");
                remaining.append(code.trim());
            }
        }

        return found ? remaining.toString() : null;
    }

    /**
     * Format backup codes as comma-separated string for storage
     * @param codes list of backup codes
     * @return comma-separated string
     */
    public String formatBackupCodesForStorage(List<String> codes) {
        return String.join(",", codes);
    }

    /**
     * Generate a random alphanumeric backup code
     */
    private String generateRandomCode(SecureRandom random) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < BACKUP_CODE_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        // Format as XXXX-XXXX for readability
        return code.substring(0, 4) + "-" + code.substring(4);
    }
}
