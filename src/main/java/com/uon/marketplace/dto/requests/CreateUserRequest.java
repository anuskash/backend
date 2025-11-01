package com.uon.marketplace.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private AppUserRequest appUser;
    private UserProfileRequest userProfile;
}
