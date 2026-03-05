package com.heavyrent.equipment.dto;

import com.heavyrent.equipment.model.EquipmentProfile;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record EquipmentProfileResponse(
        String name,
        UUID publicId,
        EquipmentProfile.EquipmentType type,
        String registrationNumber,
        String brand,
        String model,
        long pricePerHourCents,
        long yearOfManufacture,
        boolean hasOperator,
        boolean hasAccreditation,
        EquipmentProfile.DeliveryType deliveryType,
        EquipmentProfile.EquipmentStatus equipmentStatus,
        LocalDateTime availableFrom,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}


