package com.heavyrent.user.service;

import com.heavyrent.user.dto.KeycloakRequest;
import com.heavyrent.user.dto.UserProfileResponse;
import com.heavyrent.user.dto.UserUpdateRequest;
import com.heavyrent.user.model.UserProfile;
import com.heavyrent.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    UserProfileRepository repository;

    @InjectMocks
    UserProfileService service;

    // ─── createUserProfile ───────────────────────────────────────────────────

    @Test
    void createUserProfile_whenUserNotExists_shouldSaveAndReturn() {
        // ARRANGE
        KeycloakRequest request = KeycloakRequest.builder()
                .keycloakId("kc-uuid-123")
                .email("ivan@heavyrent.com")
                .firstName("Ivan")
                .lastName("Petrov")
                .phone("+7-999-000-00-00")
                .role(UserProfile.Role.RENTER)
                .build();

        when(repository.findByKeycloakId("kc-uuid-123")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        service.createUserProfile(request);

        // ASSERT
        verify(repository, times(1)).save(any());
    }

    @Test
    void createUserProfile_whenUserAlreadyExists_shouldThrow() {
        // ARRANGE
        KeycloakRequest request = KeycloakRequest.builder()
                .keycloakId("kc-uuid-123")
                .email("ivan@heavyrent.com")
                .firstName("Ivan")
                .lastName("Petrov")
                .phone("+7-999-000-00-00")
                .role(UserProfile.Role.RENTER)
                .build();

        UserProfile existing = new UserProfile();
        existing.setKeycloakId("kc-uuid-123");

        when(repository.findByKeycloakId("kc-uuid-123")).thenReturn(Optional.of(existing));

        // ACT + ASSERT
        assertThrows(DataIntegrityViolationException.class, () -> service.createUserProfile(request));
        verify(repository, never()).save(any());
    }

    // ─── getUserByUuid ───────────────────────────────────────────────────────

    @Test
    void getUserByUuid_whenUserExists_shouldReturnResponse() {
        // ARRANGE
        UUID publicId = UUID.randomUUID();

        UserProfile userProfile = new UserProfile();
        userProfile.setEmail("ivan@heavyrent.com");
        userProfile.setStatus(UserProfile.Status.ACTIVE);
        userProfile.setRole(UserProfile.Role.RENTER);

        when(repository.findByPublicId(publicId)).thenReturn(Optional.of(userProfile));

        // ACT
        UserProfileResponse result = service.getUserByUuid(publicId);

        // ASSERT
        assertEquals("ivan@heavyrent.com", result.email());
        assertEquals(UserProfile.Status.ACTIVE, result.status());
        assertEquals(UserProfile.Role.RENTER, result.role());
    }

    @Test
    void getUserByUuid_whenUserNotExists_shouldThrow() {
        // ARRANGE
        UUID publicId = UUID.randomUUID();
        when(repository.findByPublicId(publicId)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(NoSuchElementException.class, () -> service.getUserByUuid(publicId));
    }

    // ─── updateUserProfile ───────────────────────────────────────────────────

    @Test
    void updateUserProfile_whenUserExists_shouldUpdateFields() {
        // ARRANGE
        UUID publicId = UUID.randomUUID();

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("NewName")
                .lastName("NewLastName")
                .build();

        UserProfile userProfile = new UserProfile();
        userProfile.setEmail("ivan@heavyrent.com");
        userProfile.setFirstName("Ivan");
        userProfile.setLastName("Petrov");
        userProfile.setStatus(UserProfile.Status.ACTIVE);

        when(repository.findByPublicId(publicId)).thenReturn(Optional.of(userProfile));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        service.updateUserProfile(updateRequest, publicId);

        // ASSERT
        assertEquals("NewName", userProfile.getFirstName());
        assertEquals("NewLastName", userProfile.getLastName());
        verify(repository, times(1)).save(userProfile);
    }

    @Test
    void updateUserProfile_whenUserNotExists_shouldThrow() {
        // ARRANGE
        UUID publicId = UUID.randomUUID();
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("NewName")
                .lastName("NewLastName")
                .build();

        when(repository.findByPublicId(publicId)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(NoSuchElementException.class, () -> service.updateUserProfile(updateRequest, publicId));
        verify(repository, never()).save(any());
    }
}