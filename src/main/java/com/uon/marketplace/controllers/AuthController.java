package com.uon.marketplace.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uon.marketplace.dto.requests.CreateUserRequest;
import com.uon.marketplace.dto.requests.TwoFactorLoginRequest;
import com.uon.marketplace.dto.requests.VerifyEmailRequest;
import com.uon.marketplace.dto.requests.ResendVerificationRequest;
import com.uon.marketplace.dto.requests.TwoFactorSetupRequest;
import com.uon.marketplace.dto.requests.TwoFactorVerifyRequest;
import com.uon.marketplace.dto.requests.ForgotPasswordRequest;
import com.uon.marketplace.dto.requests.ResetPasswordRequest;
import com.uon.marketplace.dto.requests.UnlockAccountRequest;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.dto.responses.LoginResponse;
import com.uon.marketplace.dto.responses.TwoFactorSetupResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.services.AuthenticationService;
import com.uon.marketplace.services.LoginService;
import com.uon.marketplace.services.EmailVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and two-factor authentication endpoints")
public class AuthController {

    private final LoginService loginService;
    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final com.uon.marketplace.services.PasswordResetService passwordResetService;

    public AuthController(LoginService loginService, AuthenticationService authenticationService, EmailVerificationService emailVerificationService, com.uon.marketplace.services.PasswordResetService passwordResetService) {
        this.loginService = loginService;
        this.authenticationService = authenticationService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/login")
    @Deprecated
    public ResponseEntity<AppUser> login(@RequestParam("email") String email, @RequestParam("password") String password) {
        return loginService.authenticate(email, password);
    }

    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> registerUser(@RequestBody CreateUserRequest request) {
        try {
            AppUserResponse userResponse = loginService.registerUser(request);
            return ResponseEntity.ok(userResponse);
        } catch (com.uon.marketplace.exceptions.DuplicateEmailException ex) {
            // Handle duplicate email inline since @ControllerAdvice is disabled
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ===== Email Verification Endpoints =====
    @Operation(summary = "Verify email with code", description = "Verify a newly registered account with a 6-digit code sent via email.")
    @PostMapping(value = "/verify-email", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        Map<String, Object> body = new HashMap<>();
        try {
            // Sanitize inputs: trim/normalize email, strip non-digits and trim code
            String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
            String code = request.getCode() == null ? null : request.getCode().replaceAll("[^0-9]", "").trim();

            boolean ok = emailVerificationService.verifyCodeForUser(email, code);
            if (ok) {
                body.put("success", true);
                body.put("message", "Email verified. Your account is now active.");
                return ResponseEntity.ok(body);
            }
            body.put("success", false);
            body.put("reason", "invalid_code");
            body.put("message", "Invalid verification code.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        } catch (Exception e) {
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @Operation(summary = "Check email verification status", description = "Returns verification status and code expiry (if pending) for the given email.")
    @GetMapping(value = "/verification-status", produces = "application/json")
    public ResponseEntity<Map<String, Object>> verificationStatus(@RequestParam("email") String emailParam) {
        Map<String, Object> body = new HashMap<>();
        try {
            String email = emailParam == null ? null : emailParam.trim().toLowerCase();
            // Delegate to service via a small inline query using authentication service facilities
            // We avoid adding a new service method by reusing existing repository access patterns.
            // Fetch minimal info via AuthenticationService helper if available; else use a lightweight lambda.
            com.uon.marketplace.entities.AppUser user = authenticationService
                    .getUserByEmail(email) // we'll add a tiny helper in AuthenticationService if not present
                    ;
            Map<String, Object> data = new HashMap<>();
            boolean verified = user.getEmailVerified() != null && user.getEmailVerified();
            data.put("email", user.getEmail());
            data.put("verified", verified);
            data.put("status", user.getStatus());
            data.put("expiresAt", user.getEmailVerificationExpiresAt());
            body.put("success", true);
            body.put("data", data);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @Operation(summary = "Resend email verification code", description = "Resend a 6-digit email verification code for accounts pending verification.")
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody ResendVerificationRequest request) {
        Map<String, Object> body = new HashMap<>();
        try {
            emailVerificationService.resendIfPending(request.getEmail());
            body.put("success", true);
            body.put("message", "Verification code resent.");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    // ============= Two-Factor Authentication Endpoints =============

    @Operation(
        summary = "Login with 2FA support",
        description = "Enhanced login endpoint supporting two-factor authentication. " +
                     "If user has 2FA enabled, first call with email/password returns twoFactorRequired=true. " +
                     "Then call again with email/password/twoFactorCode to complete authentication."
    )
    @PostMapping("/login/v2")
    public ResponseEntity<LoginResponse> loginWithTwoFactor(@RequestBody TwoFactorLoginRequest request) {
        try {
            LoginResponse response = authenticationService.login(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            LoginResponse errorResponse = new LoginResponse(
                null, null, null, null, false, "Login failed: " + e.getMessage(), false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(
        summary = "Initialize 2FA setup",
        description = "Generate TOTP secret, QR code, and backup codes for user. " +
                     "User must scan QR code with authenticator app (Google Authenticator, Authy, etc.) " +
                     "and verify with a code to enable 2FA."
    )
    @PostMapping("/2fa/setup")
    public ResponseEntity<TwoFactorSetupResponse> setupTwoFactor(@RequestBody TwoFactorSetupRequest request) {
        try {
            TwoFactorSetupResponse response = authenticationService.setupTwoFactor(request.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(
        summary = "Verify and enable 2FA",
        description = "After scanning QR code, user must provide a valid 6-digit code from authenticator app " +
                     "to confirm setup and enable 2FA for their account."
    )
    @PostMapping("/2fa/verify")
    public ResponseEntity<Map<String, Object>> verifyTwoFactor(@RequestBody TwoFactorVerifyRequest request) {
        try {
            boolean verified = authenticationService.verifyAndEnableTwoFactor(request);
            Map<String, Object> response = new HashMap<>();
            
            if (verified) {
                response.put("success", true);
                response.put("message", "Two-factor authentication enabled successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid verification code");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
        summary = "Disable 2FA",
        description = "Disable two-factor authentication for user. Requires password verification for security."
    )
    @PostMapping("/2fa/disable")
    public ResponseEntity<Map<String, Object>> disableTwoFactor(
            @RequestParam Long userId,
            @RequestParam String password
    ) {
        try {
            boolean disabled = authenticationService.disableTwoFactor(userId, password);
            Map<String, Object> response = new HashMap<>();
            
            if (disabled) {
                response.put("success", true);
                response.put("message", "Two-factor authentication disabled successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to disable 2FA");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
        summary = "Regenerate backup codes",
        description = "Generate new set of backup recovery codes. Requires current 2FA code for verification. " +
                     "Old backup codes will be invalidated."
    )
    @PostMapping("/2fa/regenerate-backup-codes")
    public ResponseEntity<Map<String, Object>> regenerateBackupCodes(
            @RequestParam Long userId,
            @RequestParam String verificationCode
    ) {
        try {
            List<String> newCodes = authenticationService.regenerateBackupCodes(userId, verificationCode);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("backupCodes", newCodes);
            response.put("message", "Backup codes regenerated successfully. Save these codes securely.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
        summary = "Check 2FA status",
        description = "Check if user has two-factor authentication enabled"
    )
    @GetMapping("/2fa/status")
    public ResponseEntity<Map<String, Object>> getTwoFactorStatus(@RequestParam Long userId) {
        try {
            boolean enabled = authenticationService.isTwoFactorEnabled(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("twoFactorEnabled", enabled);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ============= Password Reset Endpoints =============

    @Operation(
        summary = "Request password reset",
        description = "Send a 6-digit password reset code to the user's email. Code expires in 15 minutes."
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Map<String, Object> body = new HashMap<>();
        try {
            passwordResetService.sendPasswordResetCode(request.getEmail());
            body.put("success", true);
            body.put("message", "Password reset code sent to your email");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @Operation(
        summary = "Reset password with code",
        description = "Reset password using the 6-digit code sent to email. Clears failed login attempts."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        Map<String, Object> body = new HashMap<>();
        try {
            boolean success = passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
            if (success) {
                body.put("success", true);
                body.put("message", "Password reset successfully");
                return ResponseEntity.ok(body);
            }
            body.put("success", false);
            body.put("message", "Invalid reset code");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        } catch (Exception e) {
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @Operation(
        summary = "Unlock account",
        description = "Unlock account after failed login attempts using the 6-digit code sent to email."
    )
    @PostMapping("/unlock-account")
    public ResponseEntity<Map<String, Object>> unlockAccount(@RequestBody UnlockAccountRequest request) {
        Map<String, Object> body = new HashMap<>();
        try {
            boolean success = passwordResetService.unlockAccount(request.getEmail(), request.getCode());
            if (success) {
                body.put("success", true);
                body.put("message", "Account unlocked successfully");
                return ResponseEntity.ok(body);
            }
            body.put("success", false);
            body.put("message", "Invalid unlock code");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        } catch (Exception e) {
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}
