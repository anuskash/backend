package com.uon.marketplace.services;

import java.lang.StackWalker.Option;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.utils.PasswordHashUtil;

@Service
public class LoginService {

    private final AppUserService appUserService;

    public LoginService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }
    public ResponseEntity<AppUser> authenticate(String email, String password) {
        Optional<AppUser> userOpt = appUserService.findByEmail(email);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            // In a real application, use a secure password hashing mechanism
            if (user.getPasswordHash().equals(PasswordHashUtil.hashWithMD5(password))) {
                user.setPasswordHash(null); // Hide password hash
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}