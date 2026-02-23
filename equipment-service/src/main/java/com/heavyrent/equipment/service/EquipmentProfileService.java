package com.heavyrent.equipment.service;

import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.equipment.repository.EquipmentProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EquipmentProfileService {

    private final EquipmentProfileRepository equipmentProfileRepository;

    public EquipmentProfileService(EquipmentProfileRepository equipmentProfileRepository) {
        this.equipmentProfileRepository = equipmentProfileRepository;
    }

    public List<EquipmentProfileResponse> findByOwnerId(long ownerId) {
        log.info("Finding equipment by ownerId: {}", ownerId);
        List<EquipmentProfile> equipmentProfiles = equipmentProfileRepository.findByOwnerId(ownerId);
        log.info("Found {} equipment profiles for ownerId: {}", equipmentProfiles.size(), ownerId);
        return equipmentProfiles.stream()
                .map(this::toResponse)
                .toList();
    }

    public EquipmentProfileResponse findByEquipmentId(long equipmentId) {
        log.info("Finding equipment by id: {}", equipmentId);
        return toResponse(equipmentProfileRepository.findEquipmentProfileById(equipmentId).orElseThrow());
    }

    public EquipmentProfileResponse createEquipmentProfile(EquipmentProfileRequest request, long ownerId) {
        log.info("Creating equipment profile for ownerId: {}", ownerId);
        EquipmentProfile profile = new EquipmentProfile();
        profile.setOwnerId(ownerId);
        EquipmentProfileResponse response = toResponse(equipmentProfileRepository.save(fillInEquipmentProfile(profile, request)));
        log.info("Created equipment profile with id: {}", response.equipmentId());
        return response;
    }

    public EquipmentProfileResponse updateEquipmentProfile(EquipmentProfileRequest request, long equipmentId) {
        log.info("Updating equipment profile with id: {}", equipmentId);
        EquipmentProfile profile = equipmentProfileRepository.findEquipmentProfileById(equipmentId).orElseThrow();
        return toResponse(equipmentProfileRepository.save(fillInEquipmentProfile(profile, request)));
    }

    public void deleteEquipmentProfile(long equipmentId) {
        log.info("Deleting equipment profile with id: {}", equipmentId);
        equipmentProfileRepository.deleteById(equipmentId);
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
                .equipmentId(equipmentProfile.getId())
                .registrationNumber(equipmentProfile.getRegistrationNumber())
                .name(equipmentProfile.getName())
                .type(equipmentProfile.getType())
                .brand(equipmentProfile.getBrand())
                .model(equipmentProfile.getModel())
                .equipmentStatus(equipmentProfile.getEquipmentStatus())
                .pricePerHourCents(equipmentProfile.getPricePerHourCents())
                .yearOfManufacture(equipmentProfile.getYearOfManufacture())
                .ownerId(equipmentProfile.getOwnerId())
                .hasOperator(equipmentProfile.isHasOperator())
                .hasAccreditation(equipmentProfile.isHasAccreditation())
                .deliveryType(equipmentProfile.getDeliveryType())
                .availableFrom(equipmentProfile.getAvailableFrom())
                .latitude(equipmentProfile.getLatitude())
                .longitude(equipmentProfile.getLongitude())
                .createdAt(equipmentProfile.getCreatedAt())
                .updatedAt(equipmentProfile.getUpdatedAt())
                .build();
    }
}
