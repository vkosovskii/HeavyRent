package com.heavyrent.equipment.dto;

import com.heavyrent.equipment.model.EquipmentProfile;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EquipmentProfileRequest(
        String name,
        String registrationNumber,
        EquipmentProfile.EquipmentType type,
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
        Double longitude
) {
}
