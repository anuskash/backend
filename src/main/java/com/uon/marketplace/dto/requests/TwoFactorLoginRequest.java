package com.uon.marketplace.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorLoginRequest {
    private String email;
    private String password;
    private String twoFactorCode; // Optional: null if user doesn't have 2FA
}
