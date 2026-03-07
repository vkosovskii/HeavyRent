package com.heavyrent.equipment.dto;

import com.heavyrent.equipment.model.EquipmentProfile;
import lombok.Builder;

import java.util.UUID;

@Builder
public record EquipmentFilterRequest(
        UUID ownerPublicId,
        String name,
        String model,
        Integer maxPricePerHourCents,
        EquipmentProfile.EquipmentType type,
        EquipmentProfile.EquipmentStatus equipmentStatus
) {
}
