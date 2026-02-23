package com.heavyrent.user.service;

import com.heavyrent.user.dto.UserProfileResponse;
import com.heavyrent.user.dto.UserUpdateRequest;
import com.heavyrent.user.model.UserProfile;
import com.heavyrent.user.repository.UserProfileRepository;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Synchronized
    public UserProfileResponse findOrCreate(String email) {
        log.info("Find or create user by email: {}", email);
        try {
            return toResponse(userProfileRepository.findByEmail(email).orElseGet(() -> save(email)));
        } catch (DataIntegrityViolationException e) {
            log.warn("Race condition in findOrCreate for email: {}", email);
            return toResponse(userProfileRepository.findByEmail(email).orElseThrow());
        }
    }

    public UserProfileResponse findUserByEmail(String email) {
        log.info("Find user by email: {}", email);
        return toResponse(userProfileRepository.findByEmail(email).orElseThrow());
    }

    public UserProfileResponse updateUserProfile(UserUpdateRequest userProfile, String email) {
        log.info("Updating user profile for email: {}", email);
        UserProfile u = userProfileRepository.findByEmail(email).orElseThrow();
        u.setPhone(userProfile.phone());
        u.setFirstName(userProfile.firstName());
        u.setLastName(userProfile.lastName());
        return toResponse(userProfileRepository.save(u));
    }

    public UserProfileResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        return toResponse(userProfileRepository.findById(id).orElseThrow());
    }

    private UserProfile save(String email) {
        UserProfile u = new UserProfile();
        u.setEmail(email);
        return userProfileRepository.save(u);
    }

    private UserProfileResponse toResponse(UserProfile userProfile) {
        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .email(userProfile.getEmail())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .phone(userProfile.getPhone())
                .isVerified(userProfile.isVerified())
                .status(userProfile.getStatus())
                .role(userProfile.getRole())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }
}
