package com.heavyrent.user.dto;

import com.heavyrent.user.model.UserProfile;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserProfileResponse(
        long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean isVerified,
        UserProfile.Status status,
        UserProfile.Role role,
        LocalDateTime updatedAt) {
}
