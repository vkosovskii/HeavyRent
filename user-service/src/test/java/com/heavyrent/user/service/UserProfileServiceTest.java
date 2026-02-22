package com.heavyrent.user.service;

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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    UserProfileRepository repository;

    @InjectMocks
    UserProfileService service;

    @Test
    void findOrCreate_whenUserExists_shouldReturnExistingUser() {
        // ARRANGE
        UserProfile existingUser = new UserProfile();
        existingUser.setEmail("test@heavyrent.com");
        existingUser.setStatus(UserProfile.Status.ACTIVE);

        when(repository.findByEmail("test@heavyrent.com"))
                .thenReturn(Optional.of(existingUser));

        // ACT
        UserProfileResponse result = service.findOrCreate("test@heavyrent.com");

        // ASSERT
        assertEquals("test@heavyrent.com", result.email());
        assertEquals(UserProfile.Status.ACTIVE, result.status());
        verify(repository, never()).save(any());
    }

    @Test
    void findOrCreate_whenNotUserExists_shouldReturnNewUser() {
        // ARRANGE
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail("test@heavyrent.com");
        userProfile.setStatus(UserProfile.Status.UNVERIFIED);

        when(repository.save(any())).thenReturn(userProfile);

        // ACT
        UserProfileResponse result = service.findOrCreate("test@heavyrent.com");

        // ASSERT
        assertEquals("test@heavyrent.com", result.email());
        assertEquals(UserProfile.Status.UNVERIFIED, result.status());
        verify(repository, atLeastOnce()).save(any());
    }

    @Test
    void findOrCreate_whenEmailIsNull_shouldReturnUserProfile() {
        when(repository.save(any())).thenReturn(null);
        assertThrows(NullPointerException.class, () -> service.findOrCreate("null"));
        verify(repository, atLeastOnce()).save(any());
    }

    @Test
    void findOrCreate_shouldHandleRaceCondition() {
        // ARRANGE
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail("test@heavyrent.com");
        userProfile.setStatus(UserProfile.Status.UNVERIFIED);

        when(repository.findByEmail("test@heavyrent.com"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(userProfile));

        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        // ACT
        UserProfileResponse result = service.findOrCreate("test@heavyrent.com");

        // ASSERT
        assertEquals("test@heavyrent.com", result.email());
        assertEquals(UserProfile.Status.UNVERIFIED, result.status());
        verify(repository, times(2)).findByEmail("test@heavyrent.com");
    }

    @Test
    void updateUserProfile() {
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .firstName("newName")
                .lastName("newLastName")
                .phone("newPhone")
                .build();

        UserProfile userProfile = new UserProfile();
        userProfile.setEmail("test@heavyrent.com");
        userProfile.setStatus(UserProfile.Status.ACTIVE);
        userProfile.setFirstName("test");
        userProfile.setLastName("test");
        userProfile.setId(1L);

        when(repository.findByEmail("test@heavyrent.com")).thenReturn(Optional.of(userProfile));
        when(repository.save(any())).thenReturn(userProfile);

        UserProfileResponse userProfileNew = service.updateUserProfile(userUpdateRequest, "test@heavyrent.com");

        assertEquals("newName", userProfile.getFirstName());
        assertEquals("newLastName", userProfile.getLastName());
        assertEquals("newPhone", userProfile.getPhone());

        assertEquals("newName", userProfileNew.firstName());
        assertEquals("newLastName", userProfileNew.lastName());
        assertEquals("newPhone", userProfileNew.phone());
        assertEquals("test@heavyrent.com", userProfileNew.email());


    }

    @Test
    void getUserById() {
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail("test@heavyrent.com");
        userProfile.setStatus(UserProfile.Status.ACTIVE);
        userProfile.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(userProfile));
        UserProfileResponse result = service.getUserById(1L);

        assertEquals("test@heavyrent.com", result.email());
        assertEquals(UserProfile.Status.ACTIVE, result.status());
        verify(repository, atLeastOnce()).findById(1L);
    }
}