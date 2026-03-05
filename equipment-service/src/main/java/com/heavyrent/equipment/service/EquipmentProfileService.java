package com.heavyrent.equipment.service;

import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.equipment.repository.EquipmentProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Slf4j
public class EquipmentProfileService {

    private final EquipmentProfileRepository equipmentProfileRepository;

    public EquipmentProfileService(EquipmentProfileRepository equipmentProfileRepository) {
        this.equipmentProfileRepository = equipmentProfileRepository;
    }

    public List<EquipmentProfileResponse> findByOwnerId(UUID ownerId) {
        log.info("Finding equipment by ownerId: {}", ownerId);
        List<EquipmentProfile> equipmentProfiles = equipmentProfileRepository.findByOwnerId(ownerId);
        log.info("Found {} equipment profiles for ownerId: {}", equipmentProfiles.size(), ownerId);
        return equipmentProfiles.stream()
                .map(this::toResponse)
                .toList();
    }

    public EquipmentProfileResponse findByEquipmentId(UUID equipmentId) {
        log.info("Finding equipment by id: {}", equipmentId);
        return toResponse(equipmentProfileRepository.findEquipmentProfileById(equipmentId)
                .orElseThrow(() -> new NoSuchElementException("Equipment not found: " + equipmentId)));
    }

    public EquipmentProfileResponse createEquipmentProfile(EquipmentProfileRequest request, UUID ownerId) {
        log.info("Creating equipment profile for ownerId: {}", ownerId);
        EquipmentProfile profile = new EquipmentProfile();
        profile.setOwnerId(ownerId);
        EquipmentProfileResponse response = toResponse(equipmentProfileRepository.save(fillInEquipmentProfile(profile, request)));
        log.info("Created equipment profile with id: {}", response.publicId());
        return response;
    }

    public EquipmentProfileResponse updateEquipmentProfile(EquipmentProfileRequest request, UUID equipmentId) {
        log.info("Updating equipment profile with id: {}", equipmentId);
        EquipmentProfile profile = equipmentProfileRepository.findEquipmentProfileById(equipmentId)
                .orElseThrow(() -> new NoSuchElementException("Equipment not found: " + equipmentId));
        return toResponse(equipmentProfileRepository.save(fillInEquipmentProfile(profile, request)));
    }

    public void deleteEquipmentProfile(UUID equipmentId) {
        log.info("Deleting equipment profile with id: {}", equipmentId);
        equipmentProfileRepository.deleteByPublicId(equipmentId);
        log.info("Deleted equipment profile with id: {}", equipmentId);
    }

    private EquipmentProfile fillInEquipmentProfile(EquipmentProfile profile, EquipmentProfileRequest equipmentProfileResponse) {
        profile.setRegistrationNumber(equipmentProfileResponse.registrationNumber());
        profile.setName(equipmentProfileResponse.name());
        profile.setType(equipmentProfileResponse.type());
        profile.setBrand(equipmentProfileResponse.brand());
        profile.setModel(equipmentProfileResponse.model());
        profile.setPricePerHourCents(equipmentProfileResponse.pricePerHourCents());
        profile.setYearOfManufacture(equipmentProfileResponse.yearOfManufacture());
        profile.setHasOperator(equipmentProfileResponse.hasOperator());
        profile.setHasAccreditation(equipmentProfileResponse.hasAccreditation());
        profile.setDeliveryType(equipmentProfileResponse.deliveryType());
        profile.setEquipmentStatus(equipmentProfileResponse.equipmentStatus());
        profile.setAvailableFrom(equipmentProfileResponse.availableFrom());
        profile.setLatitude(equipmentProfileResponse.latitude());
        profile.setLongitude(equipmentProfileResponse.longitude());
        return profile;
    }

    private EquipmentProfileResponse toResponse(EquipmentProfile equipmentProfile) {
        return EquipmentProfileResponse.builder()
                .name(equipmentProfile.getName())
                .publicId(equipmentProfile.getPublicId())
                .type(equipmentProfile.getType())
                .registrationNumber(equipmentProfile.getRegistrationNumber())
                .brand(equipmentProfile.getBrand())
                .model(equipmentProfile.getModel())
                .pricePerHourCents(equipmentProfile.getPricePerHourCents())
                .yearOfManufacture(equipmentProfile.getYearOfManufacture())
                .hasOperator(equipmentProfile.isHasOperator())
                .hasAccreditation(equipmentProfile.isHasAccreditation())
                .deliveryType(equipmentProfile.getDeliveryType())
                .equipmentStatus(equipmentProfile.getEquipmentStatus())
                .availableFrom(equipmentProfile.getAvailableFrom())
                .latitude(equipmentProfile.getLatitude())
                .longitude(equipmentProfile.getLongitude())
                .createdAt(equipmentProfile.getCreatedAt())
                .updatedAt(equipmentProfile.getUpdatedAt())
                .build();
    }
}
