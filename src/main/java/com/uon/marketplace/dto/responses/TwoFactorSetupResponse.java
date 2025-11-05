package com.uon.marketplace.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorSetupResponse {
    private String secret; // TOTP secret (never expose after initial setup)
    private String qrCodeUrl; // Data URL for QR code image
    private String manualEntryKey; // For manual entry in authenticator apps
    private String issuer; // "UON Marketplace"
    private String accountName; // User's email
    private List<String> backupCodes; // One-time use recovery codes
}
