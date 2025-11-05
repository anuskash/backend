package com.uon.marketplace.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketPlaceUser {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role; // user/admin
    private String status; // active, Pending Verification, banned, etc.
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
}
