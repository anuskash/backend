package com.uon.marketplace.services;

import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserService {
    @Autowired
    private AppUserRepository appUserRepository;

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    public AppUser createUser(AppUser user) {
        return appUserRepository.save(user);
    }

    public AppUser updateUser(Long id, AppUser userDetails) {
        return appUserRepository.findById(id)
                .map(user -> {
                    user.setRole(userDetails.getRole());
                    user.setPasswordHash(userDetails.getPasswordHash());
                    user.setStatus(userDetails.getStatus());
                    return appUserRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        appUserRepository.deleteById(id);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
    //find by id
    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }
}
