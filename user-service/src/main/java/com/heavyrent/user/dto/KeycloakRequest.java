package com.heavyrent.user.dto;


import com.heavyrent.user.model.UserProfile;
import lombok.Builder;

@Builder
public record KeycloakRequest(
        String keycloakId,
        String email,
        String firstName,
        String lastName,
        String phone,
        UserProfile.Role role
) {
}
