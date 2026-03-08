package com.heavyrent.equipment.service;

import com.heavyrent.equipment.dto.EquipmentFilterRequest;
import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.mapper.EquipmentEntityMapper;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.equipment.repository.EquipmentProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.heavyrent.equipment.mapper.EquipmentEntityMapper.toEntity;
import static com.heavyrent.equipment.mapper.EquipmentEntityMapper.toResponse;
import static com.heavyrent.equipment.specification.EquipmentSpecification.*;

@Service
@Slf4j
public class EquipmentProfileService {

    private final EquipmentProfileRepository equipmentProfileRepository;

    public EquipmentProfileService(EquipmentProfileRepository equipmentProfileRepository) {
        this.equipmentProfileRepository = equipmentProfileRepository;
    }

    /// For inter-service use only!!
    /// This key should not be shown to the client.
    public List<EquipmentProfileResponse> findByOwnerKeycloakId(UUID ownerKeycloakId) {
        log.info("Finding equipment by owner Keycloak ID: {}", ownerKeycloakId);
        List<EquipmentProfile> equipmentProfiles = equipmentProfileRepository.findByOwnerKeycloakId(ownerKeycloakId);
        log.info("Found {} equipment profiles for ownerId: {}", equipmentProfiles.size(), ownerKeycloakId);
        return equipmentProfiles.stream()
                .map(EquipmentEntityMapper::toResponse)
                .toList();
    }

    public EquipmentProfileResponse findByEquipmentId(UUID equipmentId) {
        log.info("Finding equipment by id: {}", equipmentId);
        return toResponse(equipmentProfileRepository.findEquipmentByPublicId(equipmentId)
                .orElseThrow(() -> new NoSuchElementException("Equipment not found: " + equipmentId)));
    }

    public EquipmentProfileResponse createEquipmentProfile(EquipmentProfileRequest request, UUID ownerKeycloakId, UUID ownerPublicId) {
        log.info("Creating equipment profile for ownerId: {}", ownerPublicId);
        EquipmentProfile profile = toEntity(request);
        profile.setOwnerKeycloakId(ownerKeycloakId);
        profile.setOwnerPublicId(ownerPublicId);
        EquipmentProfileResponse response = toResponse(equipmentProfileRepository.save(profile));
        log.info("Created equipment profile with id: {}", response.publicId());
        return response;
    }

    public EquipmentProfileResponse updateEquipmentProfile(EquipmentProfileRequest request, UUID equipmentId) {
        log.info("Updating equipment profile with id: {}", equipmentId);
        EquipmentProfile profile = equipmentProfileRepository.findEquipmentByPublicId(equipmentId)
                .orElseThrow(() -> new NoSuchElementException("Equipment not found: " + equipmentId));
        return toResponse(equipmentProfileRepository.save(toEntity(request, profile)));
    }

    public void deleteEquipmentProfile(UUID equipmentId) {
        log.info("Deleting equipment profile with id: {}", equipmentId);
        equipmentProfileRepository.deleteByPublicId(equipmentId);
        log.info("Deleted equipment profile with id: {}", equipmentId);
    }

    public Page<EquipmentProfileResponse> findAll(EquipmentFilterRequest filter, int page, int pageSize) {
        log.info("Finding all equipment profiles");
        Pageable pageable = PageRequest.of(page, pageSize);
        Specification<EquipmentProfile> spec = Specification
                .where(byOwnerPublicId(filter.ownerPublicId()))
                .and(byName(filter.name()))
                .and(byModel(filter.model()))
                .and(byType(filter.type()))
                .and(byStatus(filter.equipmentStatus()))
                .and(byMaxPrice(filter.maxPricePerHourCents()));
        return equipmentProfileRepository.findAll(spec, pageable).map(EquipmentEntityMapper::toResponse);
    }
}
