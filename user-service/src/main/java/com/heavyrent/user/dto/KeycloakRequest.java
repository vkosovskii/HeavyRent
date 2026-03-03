package com.heavyrent.user.dto;


import com.heavyrent.user.model.UserProfile;
import lombok.Builder;

import java.util.UUID;

@Builder
public record KeycloakRequest(
        UUID keycloakId,
        String email,
        String firstName,
        String lastName,
        String phone,
        UserProfile.Role role
) {
}
