package com.uon.marketplace.services;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.uon.marketplace.dto.requests.AppUserRequest;
import com.uon.marketplace.dto.requests.CreateUserRequest;
import com.uon.marketplace.dto.requests.UserProfileRequest;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.Role;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.utils.PasswordHashUtil;

@Service
public class LoginService {

    private final AppUserService appUserService;
    private final UserProfileService userProfileService;
    private final EmailVerificationService emailVerificationService;

    public LoginService(AppUserService appUserService, UserProfileService userProfileService, EmailVerificationService emailVerificationService) {
        this.appUserService = appUserService;
        this.userProfileService = userProfileService;
        this.emailVerificationService = emailVerificationService;
    }
    public ResponseEntity<AppUser> authenticate(String email, String password) {
        Optional<AppUser> userOpt = appUserService.findByEmail(email);
        System.out.println("Attempting login for email: " + email);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            System.out.println("Found user: " + user.getEmail());
            System.out.println("User status: " + user.getStatus());
            if (!"active".equalsIgnoreCase(user.getStatus())) {
                System.out.println("Account not active - pending verification or banned");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            
            String hashedInputPassword = PasswordHashUtil.hashWithMD5(password);
            System.out.println("Input password hash: " + hashedInputPassword);
            System.out.println("Stored password hash: " + user.getPasswordHash());
            
            // In a real application, use a secure password hashing mechanism
            if (user.getPasswordHash().equals(hashedInputPassword)) {
                System.out.println("Password match - login successful");
                user.setPasswordHash(null); // Hide password hash
                return ResponseEntity.ok(user);
            } else {
                System.out.println("Password mismatch - login failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } else {
            System.out.println("User not found for email: " + email);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    public AppUserResponse registerUser(CreateUserRequest request) {
        AppUserRequest appUserReq = request.getAppUser();

        // Normalize and validate email
        String email = appUserReq.getEmail() == null ? null : appUserReq.getEmail().trim().toLowerCase();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        // Friendly duplicate check before hitting DB constraint
        if (appUserService.findByEmail(email).isPresent()) {
            throw new com.uon.marketplace.exceptions.DuplicateEmailException("Email already registered");
        }

        AppUser appUser = new AppUser();
        appUser.setRole(Role.USER); // Set role to USER enum
        appUser.setPasswordHash(PasswordHashUtil.hashWithMD5(appUserReq.getPassword())); // Hash in real app
        appUser.setStatus("Pending Verification"); // Set status as Pending Verification
        appUser.setEmail(email);
        appUser.setCreatedAt(java.time.LocalDateTime.now());
        appUser = appUserService.createUser(appUser);

        UserProfileRequest profileReq = request.getUserProfile();
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(appUser.getUserId());
        userProfile.setFirstName(profileReq.getFirstName());
        userProfile.setLastName(profileReq.getLastName());
        userProfile.setPhoneNumber(profileReq.getPhoneNumber());
        userProfile.setProfileImageUrl(profileReq.getProfileImageUrl());
        userProfileService.createProfile(userProfile);

        // Kick off email verification (send 6-digit code)
        emailVerificationService.createAndSendVerificationCode(appUser);

        return new AppUserResponse(appUser, userProfile);
    }
}