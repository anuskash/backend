package com.uon.marketplace.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uon.marketplace.dto.requests.CreateUserRequest;
import com.uon.marketplace.dto.responses.AppUserResponse;
import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.services.LoginService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public ResponseEntity<AppUser> login(@RequestParam("email") String email, @RequestParam("password") String password) {
        return loginService.authenticate(email, password);
    }

    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> registerUser(@RequestBody CreateUserRequest request) {
        AppUserResponse userResponse = loginService.registerUser(request);
        return ResponseEntity.ok(userResponse);
    }
}
