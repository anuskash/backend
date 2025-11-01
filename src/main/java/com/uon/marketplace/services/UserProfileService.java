package com.uon.marketplace.services;

import com.uon.marketplace.entities.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uon.marketplace.repositories.UserProfileRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {
    @Autowired
    private UserProfileRepository userProfileRepository;

    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll();
    }

    public Optional<UserProfile> getProfileById(Long id) {
        return userProfileRepository.findById(id);
    }

    public UserProfile createProfile(UserProfile profile) {
        return userProfileRepository.save(profile);
    }

    public UserProfile updateProfile(Long id, UserProfile profileDetails) {
        return userProfileRepository.findById(id)
                .map(profile -> {
                    profile.setUserId(profileDetails.getUserId());
                    profile.setFirstName(profileDetails.getFirstName());
                    profile.setLastName(profileDetails.getLastName());
                    profile.setPhoneNumber(profileDetails.getPhoneNumber());
                    profile.setProfileImageUrl(profileDetails.getProfileImageUrl());
                    return userProfileRepository.save(profile);
                })
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public void deleteProfile(Long id) {
        userProfileRepository.deleteById(id);
    }

    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));
    }
}
