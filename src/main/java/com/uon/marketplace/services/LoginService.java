package com.uon.marketplace.services;

import java.lang.StackWalker.Option;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.uon.marketplace.dto.requests.AppUserRequest;
import com.uon.marketplace.dto.requests.CreateUserRequest;
import com.uon.marketplace.dto.requests.UserProfileRequest;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.UserProfile;
import com.uon.marketplace.utils.PasswordHashUtil;

@Service
public class LoginService {

    private final AppUserService appUserService;
    private final UserProfileService userProfileService;

    public LoginService(AppUserService appUserService, UserProfileService userProfileService) {
        this.appUserService = appUserService;
        this.userProfileService = userProfileService;
    }
    public ResponseEntity<AppUser> authenticate(String email, String password) {
        Optional<AppUser> userOpt = appUserService.findByEmail(email);
        System.out.println("Attempting login for email: " + email);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            System.out.println("Found user: " + user.getEmail());
            System.out.println("User status: " + user.getStatus());
            
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
        AppUser appUser = new AppUser();
        appUser.setRole("user");
        appUser.setPasswordHash(PasswordHashUtil.hashWithMD5(appUserReq.getPassword())); // Hash in real app
        appUser.setStatus("Pending Verification"); // Set status as Pending Verification instead of active
        appUser.setEmail(appUserReq.getEmail());
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
        return new AppUserResponse(appUser, userProfile);
    }
}