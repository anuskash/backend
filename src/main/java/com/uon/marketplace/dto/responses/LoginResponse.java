package com.uon.marketplace.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Long userId;
    private String email;
    private String role;
    private String token; // JWT token (null if 2FA required)
    private boolean twoFactorRequired; // True if user has 2FA enabled
    private String message;
    private boolean success;
}
