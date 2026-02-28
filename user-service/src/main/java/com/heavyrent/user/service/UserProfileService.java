package com.heavyrent.user.service;

import com.heavyrent.user.dto.KeycloakRequest;
import com.heavyrent.user.dto.UserProfileResponse;
import com.heavyrent.user.dto.UserUpdateRequest;
import com.heavyrent.user.model.UserProfile;
import com.heavyrent.user.repository.UserProfileRepository;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Synchronized
    public void createUserProfile(KeycloakRequest request) {
        log.info("Find or create user by keycloakId: {}", request);
        UserProfile userProfile = userProfileRepository.findByKeycloakId(request.keycloakId()).orElse(null);
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setKeycloakId(request.keycloakId());
            userProfile.setFirstName(request.firstName());
            userProfile.setLastName(request.lastName());
            userProfile.setEmail(request.email());
            userProfile.setPhone(request.phone());
            userProfile.setRole(request.role());
            userProfileRepository.save(userProfile);
        } else {
            log.error("User profile already exists: {}", userProfile);
            throw new DataIntegrityViolationException("User profile already exists");
        }
    }

    public UserProfileResponse getUserByUuid(UUID publicId) {
        log.info("Find or get user by public id: {}", publicId);
        UserProfile userProfile = userProfileRepository.findByPublicId(publicId).orElseThrow(NoSuchElementException::new);
        return toResponse(userProfile);
    }

    public void updateUserProfile(UserUpdateRequest updateRequest, UUID publicId) {
        log.info("Update user profile: {}", updateRequest);
        UserProfile userProfile = userProfileRepository.findByPublicId(publicId).orElseThrow(NoSuchElementException::new);
        userProfile.setLastName(updateRequest.lastName());
        userProfile.setFirstName(updateRequest.firstName());
        userProfileRepository.save(userProfile);
    }

    // TODO need verify service
    public void requestContactChange() {
        log.info("Request contact change");
    }

    // TODO need verify service
    public void requestRoleChange() {
        log.info("Request role change");
    }

    // TODO need verify service
    public void updateStatus() {
        log.info("Update status");
    }

    private UserProfileResponse toResponse(UserProfile userProfile) {
        return UserProfileResponse.builder()
                .publicId(userProfile.getPublicId())
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
