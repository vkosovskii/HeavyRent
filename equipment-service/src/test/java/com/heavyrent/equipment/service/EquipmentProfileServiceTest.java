package com.heavyrent.equipment.service;


import com.heavyrent.equipment.base.BaseTest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.equipment.repository.EquipmentProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EquipmentProfileServiceTest extends BaseTest {

    @Mock
    EquipmentProfileRepository repository;

    @InjectMocks
    EquipmentProfileService service;

    @Test
    public void findByEquipmentId_shouldFindByOwnerId() {
        when(repository.findEquipmentProfileById(1L))
                .thenReturn(Optional.of(getEquipmentProfile()));
        EquipmentProfileResponse response = service.findByEquipmentId(1L);
        assertEquals(1L, response.equipmentId());
        verify(repository, atLeastOnce()).findEquipmentProfileById(1L);
    }

    @Test
    public void findByOwnerId_shouldFindList() {
        when(repository.findByOwnerId(1L))
                .thenReturn(getEquipmentProfileList());
        List<EquipmentProfileResponse> result = service.findByOwnerId(1L);
        assertEquals(1, result.size());
        verify(repository, atLeastOnce()).findByOwnerId(1L);
    }

    @Test
    public void createEquipmentProfile() {
        when(repository.save(any())).thenReturn(getEquipmentProfile());

        service.createEquipmentProfile(getEquipmentProfileRequest(), 1L);

        ArgumentCaptor<EquipmentProfile> captor = ArgumentCaptor.forClass(EquipmentProfile.class);
        verify(repository).save(captor.capture());
        assertEquals("А123БВ777", captor.getValue().getRegistrationNumber());
        assertEquals(1L, captor.getValue().getOwnerId());
    }

    @Test
    public void updateEquipmentProfile() {
        when(repository.findEquipmentProfileById(1L))
                .thenReturn(Optional.of(getEquipmentProfile()));
        when(repository.save(any()))
                .thenReturn(getEquipmentProfile());
        EquipmentProfileResponse response = service.updateEquipmentProfile(getEquipmentProfileRequest(), 1L);
        assertEquals(1L, response.equipmentId());
        verify(repository, atLeastOnce()).findEquipmentProfileById(1L);
        verify(repository, atLeastOnce()).save(any());
    }

    @Test
    public void deleteEquipmentProfile() {
        service.deleteEquipmentProfile(1L);
        verify(repository, atLeastOnce()).deleteById(1L);
    }

    @Test
    public void findByEquipmentId_whenNotFound_shouldThrowException() {
        when(repository.findEquipmentProfileById(1L))
                .thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.findByEquipmentId(1L));
    }

    @Test
    public void findByOwnerId_whenNoEquipment_shouldReturnEmptyList() {
        when(repository.findByOwnerId(1L))
                .thenReturn(Collections.emptyList());
        List<EquipmentProfileResponse> result = service.findByOwnerId(1L);
        assertEquals(0, result.size());
        verify(repository, atLeastOnce()).findByOwnerId(1L);
    }

    @Test
    public void updateEquipmentProfile_whenNotFound_shouldThrowException() {
        when(repository.findEquipmentProfileById(1L))
                .thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.updateEquipmentProfile(getEquipmentProfileRequest(), 1L));
    }

    @Test
    public void createEquipmentProfile_whenSaveFails_shouldThrowException() {
        when(repository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        assertThrows(DataIntegrityViolationException.class,
                () -> service.createEquipmentProfile(getEquipmentProfileRequest(), 1L));
    }
}
