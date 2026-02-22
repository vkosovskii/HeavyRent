package com.heavyrent.user.controller;

import com.heavyrent.user.dto.UserProfileResponse;
import com.heavyrent.user.dto.UserUpdateRequest;
import com.heavyrent.user.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserProfileController {

    UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/api/users/me")
    public UserProfileResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        return userProfileService.findOrCreate(getUserEmail(jwt));
    }

    @PutMapping("/api/users/me")
    public UserProfileResponse putMe(@RequestBody UserUpdateRequest userProfile, @AuthenticationPrincipal Jwt jwt) {
        return userProfileService.updateUserProfile(userProfile, getUserEmail(jwt));
    }

    @GetMapping("/api/users/{id}")
    public UserProfileResponse getUser(@PathVariable("id") Long id) {
        return userProfileService.getUserById(id);
    }

    private String getUserEmail(Jwt jwt) {
        return jwt.getSubject();
    }
}
