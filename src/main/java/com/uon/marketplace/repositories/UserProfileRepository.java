package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.UserProfile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);
    
    // Batch fetch profiles by user IDs
    List<UserProfile> findByUserIdIn(List<Long> userIds);
}
