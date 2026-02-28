package com.heavyrent.user.dto;

import lombok.Builder;

@Builder
public record UserUpdateRequest(
        String firstName,
        String lastName
) {
}
