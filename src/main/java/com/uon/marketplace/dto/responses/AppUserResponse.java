package com.uon.marketplace.dto.responses;

import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUserResponse {
    private AppUser appUser;
    private UserProfile userProfile;
    
}
