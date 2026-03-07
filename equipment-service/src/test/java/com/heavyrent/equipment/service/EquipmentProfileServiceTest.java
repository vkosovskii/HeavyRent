package com.heavyrent.equipment.service;

import com.heavyrent.equipment.base.BaseTest;
import com.heavyrent.equipment.dto.EquipmentFilterRequest;
import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.equipment.repository.EquipmentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EquipmentProfileServiceTest extends BaseTest {

    @Mock
    private EquipmentProfileRepository equipmentProfileRepository;

    @InjectMocks
    private EquipmentProfileService equipmentProfileService;

    private EquipmentProfile equipmentProfile;
    private EquipmentProfileRequest equipmentProfileRequest;

    @BeforeEach
    void setUp() {
        equipmentProfile = getEquipmentProfile();
        equipmentProfileRequest = getEquipmentProfileRequest();

        equipmentProfile.setPublicId(equipmentId);
        equipmentProfile.setOwnerPublicId(UUID.randomUUID());
    }

    @Test
    void findByOwnerKeycloakId_shouldReturnMappedResponses() {
        UUID ownerKeycloakId = UUID.randomUUID();
        equipmentProfile.setOwnerKeycloakId(ownerKeycloakId);

        when(equipmentProfileRepository.findByOwnerKeycloakId(ownerKeycloakId))
                .thenReturn(List.of(equipmentProfile));

        List<EquipmentProfileResponse> result = equipmentProfileService.findByOwnerKeycloakId(ownerKeycloakId);

        assertNotNull(result);
        assertEquals(1, result.size());

        EquipmentProfileResponse response = result.getFirst();
        assertEquals(equipmentProfile.getPublicId(), response.publicId());
        assertEquals(equipmentProfile.getName(), response.name());
        assertEquals(equipmentProfile.getType(), response.type());
        assertEquals(equipmentProfile.getBrand(), response.brand());
        assertEquals(equipmentProfile.getModel(), response.model());
        assertEquals(equipmentProfile.getPricePerHourCents(), response.pricePerHourCents());
        assertEquals(equipmentProfile.getYearOfManufacture(), response.yearOfManufacture());
        assertEquals(equipmentProfile.isHasOperator(), response.hasOperator());
        assertEquals(equipmentProfile.isHasAccreditation(), response.hasAccreditation());
        assertEquals(equipmentProfile.getDeliveryType(), response.deliveryType());
        assertEquals(equipmentProfile.getEquipmentStatus(), response.equipmentStatus());
        assertEquals(equipmentProfile.getLatitude(), response.latitude());
        assertEquals(equipmentProfile.getLongitude(), response.longitude());

        verify(equipmentProfileRepository).findByOwnerKeycloakId(ownerKeycloakId);
    }

    @Test
    void findByOwnerKeycloakId_shouldReturnEmptyList_whenNothingFound() {
        UUID ownerKeycloakId = UUID.randomUUID();

        when(equipmentProfileRepository.findByOwnerKeycloakId(ownerKeycloakId))
                .thenReturn(Collections.emptyList());

        List<EquipmentProfileResponse> result = equipmentProfileService.findByOwnerKeycloakId(ownerKeycloakId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(equipmentProfileRepository).findByOwnerKeycloakId(ownerKeycloakId);
    }

    @Test
    void findByEquipmentId_shouldReturnMappedResponse() {
        UUID equipmentPublicId = UUID.randomUUID();
        equipmentProfile.setPublicId(equipmentPublicId);

        when(equipmentProfileRepository.findEquipmentPublicById(equipmentPublicId))
                .thenReturn(Optional.of(equipmentProfile));

        EquipmentProfileResponse result = equipmentProfileService.findByEquipmentId(equipmentPublicId);

        assertNotNull(result);
        assertEquals(equipmentProfile.getPublicId(), result.publicId());
        assertEquals(equipmentProfile.getRegistrationNumber(), result.registrationNumber());
        assertEquals(equipmentProfile.getName(), result.name());
        assertEquals(equipmentProfile.getType(), result.type());
        assertEquals(equipmentProfile.getBrand(), result.brand());
        assertEquals(equipmentProfile.getModel(), result.model());
    }

    @Test
    void findByEquipmentId_shouldThrow_whenEquipmentNotFound() {
        UUID equipmentPublicId = UUID.randomUUID();

        when(equipmentProfileRepository.findEquipmentPublicById(equipmentPublicId))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> equipmentProfileService.findByEquipmentId(equipmentPublicId)
        );

        assertEquals("Equipment not found: " + equipmentPublicId, exception.getMessage());
        verify(equipmentProfileRepository).findEquipmentPublicById(equipmentPublicId);
    }

    @Test
    void createEquipmentProfile_shouldFillProfileAndReturnResponse() {
        UUID ownerKeycloakId = UUID.randomUUID();
        UUID ownerPublicId = UUID.randomUUID();

        EquipmentProfile savedProfile = getEquipmentProfile();
        savedProfile.setPublicId(UUID.randomUUID());
        savedProfile.setOwnerKeycloakId(ownerKeycloakId);
        savedProfile.setOwnerPublicId(ownerPublicId);

        when(equipmentProfileRepository.save(any(EquipmentProfile.class)))
                .thenReturn(savedProfile);

        EquipmentProfileResponse result = equipmentProfileService.createEquipmentProfile(
                equipmentProfileRequest,
                ownerKeycloakId,
                ownerPublicId
        );

        assertNotNull(result);
        assertEquals(savedProfile.getPublicId(), result.publicId());

        ArgumentCaptor<EquipmentProfile> captor = ArgumentCaptor.forClass(EquipmentProfile.class);
        verify(equipmentProfileRepository).save(captor.capture());

        EquipmentProfile captured = captor.getValue();
        assertEquals(ownerKeycloakId, captured.getOwnerKeycloakId());
        assertEquals(ownerPublicId, captured.getOwnerPublicId());
        assertEquals(equipmentProfileRequest.registrationNumber(), captured.getRegistrationNumber());
        assertEquals(equipmentProfileRequest.name(), captured.getName());
        assertEquals(equipmentProfileRequest.type(), captured.getType());
        assertEquals(equipmentProfileRequest.brand(), captured.getBrand());
        assertEquals(equipmentProfileRequest.model(), captured.getModel());
        assertEquals(equipmentProfileRequest.pricePerHourCents(), captured.getPricePerHourCents());
        assertEquals(equipmentProfileRequest.yearOfManufacture(), captured.getYearOfManufacture());
        assertEquals(equipmentProfileRequest.hasOperator(), captured.isHasOperator());
        assertEquals(equipmentProfileRequest.hasAccreditation(), captured.isHasAccreditation());
        assertEquals(equipmentProfileRequest.deliveryType(), captured.getDeliveryType());
        assertEquals(equipmentProfileRequest.equipmentStatus(), captured.getEquipmentStatus());
        assertEquals(equipmentProfileRequest.availableFrom(), captured.getAvailableFrom());
        assertEquals(equipmentProfileRequest.latitude(), captured.getLatitude());
        assertEquals(equipmentProfileRequest.longitude(), captured.getLongitude());
    }

    @Test
    void updateEquipmentProfile_shouldUpdateExistingProfileAndReturnResponse() {
        UUID equipmentPublicId = UUID.randomUUID();
        equipmentProfile.setPublicId(equipmentPublicId);

        when(equipmentProfileRepository.findEquipmentPublicById(equipmentPublicId))
                .thenReturn(Optional.of(equipmentProfile));
        when(equipmentProfileRepository.save(any(EquipmentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EquipmentProfileResponse result = equipmentProfileService.updateEquipmentProfile(
                equipmentProfileRequest,
                equipmentPublicId
        );

        assertNotNull(result);
        assertEquals(equipmentProfileRequest.name(), result.name());
        assertEquals(equipmentProfileRequest.registrationNumber(), result.registrationNumber());
        assertEquals(equipmentProfileRequest.type(), result.type());
        assertEquals(equipmentProfileRequest.brand(), result.brand());
        assertEquals(equipmentProfileRequest.model(), result.model());

        ArgumentCaptor<EquipmentProfile> captor = ArgumentCaptor.forClass(EquipmentProfile.class);
        verify(equipmentProfileRepository).save(captor.capture());

        EquipmentProfile updated = captor.getValue();
        assertEquals(equipmentProfileRequest.registrationNumber(), updated.getRegistrationNumber());
        assertEquals(equipmentProfileRequest.name(), updated.getName());
        assertEquals(equipmentProfileRequest.type(), updated.getType());
        assertEquals(equipmentProfileRequest.brand(), updated.getBrand());
        assertEquals(equipmentProfileRequest.model(), updated.getModel());
        assertEquals(equipmentProfileRequest.pricePerHourCents(), updated.getPricePerHourCents());
        assertEquals(equipmentProfileRequest.yearOfManufacture(), updated.getYearOfManufacture());
        assertEquals(equipmentProfileRequest.hasOperator(), updated.isHasOperator());
        assertEquals(equipmentProfileRequest.hasAccreditation(), updated.isHasAccreditation());
        assertEquals(equipmentProfileRequest.deliveryType(), updated.getDeliveryType());
        assertEquals(equipmentProfileRequest.equipmentStatus(), updated.getEquipmentStatus());
        assertEquals(equipmentProfileRequest.availableFrom(), updated.getAvailableFrom());
        assertEquals(equipmentProfileRequest.latitude(), updated.getLatitude());
        assertEquals(equipmentProfileRequest.longitude(), updated.getLongitude());
    }

    @Test
    void updateEquipmentProfile_shouldThrow_whenEquipmentNotFound() {
        UUID equipmentPublicId = UUID.randomUUID();

        when(equipmentProfileRepository.findEquipmentPublicById(equipmentPublicId))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> equipmentProfileService.updateEquipmentProfile(equipmentProfileRequest, equipmentPublicId)
        );

        assertEquals("Equipment not found: " + equipmentPublicId, exception.getMessage());
        verify(equipmentProfileRepository).findEquipmentPublicById(equipmentPublicId);
        verify(equipmentProfileRepository, never()).save(any());
    }

    @Test
    void deleteEquipmentProfile_shouldCallRepository() {
        UUID equipmentPublicId = UUID.randomUUID();

        doNothing().when(equipmentProfileRepository).deleteByPublicId(equipmentPublicId);

        assertDoesNotThrow(() -> equipmentProfileService.deleteEquipmentProfile(equipmentPublicId));

        verify(equipmentProfileRepository).deleteByPublicId(equipmentPublicId);
    }

    @Test
    void findAll_shouldReturnMappedPage() {
        UUID ownerPublicId = UUID.randomUUID();

        EquipmentFilterRequest filter = new EquipmentFilterRequest(
                ownerPublicId,
                "KAMAZ",
                "320D",
                200000,
                EquipmentProfile.EquipmentType.EXCAVATOR,
                EquipmentProfile.EquipmentStatus.FREE
        );

        Page<EquipmentProfile> entityPage = new PageImpl<>(
                List.of(equipmentProfile),
                PageRequest.of(0, 10),
                1
        );

        when(equipmentProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        Page<EquipmentProfileResponse> result = equipmentProfileService.findAll(filter, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        EquipmentProfileResponse response = result.getContent().getFirst();
        assertEquals(equipmentProfile.getPublicId(), response.publicId());
        assertEquals(equipmentProfile.getName(), response.name());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(equipmentProfileRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenRepositoryReturnsNoData() {
        EquipmentFilterRequest filter = new EquipmentFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(equipmentProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<EquipmentProfileResponse> result = equipmentProfileService.findAll(filter, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(equipmentProfileRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_shouldThrow_whenPageIsNegative() {
        EquipmentFilterRequest filter = new EquipmentFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> equipmentProfileService.findAll(filter, -1, 10));

        verify(equipmentProfileRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_shouldThrow_whenPageSizeIsZero() {
        EquipmentFilterRequest filter = new EquipmentFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> equipmentProfileService.findAll(filter, 0, 0));

        verify(equipmentProfileRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }
}