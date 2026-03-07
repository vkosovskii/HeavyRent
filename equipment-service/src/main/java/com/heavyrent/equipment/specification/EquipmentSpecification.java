package com.heavyrent.equipment.specification;

import com.heavyrent.equipment.model.EquipmentProfile;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class EquipmentSpecification {
    public static Specification<EquipmentProfile> byOwnerKeycloakId(UUID ownerKeycloakId) {
        return (root, query, cb) -> {
            if (ownerKeycloakId == null) return null;
            return cb.equal(root.get("ownerKeycloakId"), ownerKeycloakId);
        };
    }
    public static Specification<EquipmentProfile> byOwnerPublicId(UUID ownerPublicId) {
        return (root, query, cb) -> {
            if (ownerPublicId == null) return null;
            return cb.equal(root.get("ownerPublicId"), ownerPublicId);
        };
    }
    public static Specification<EquipmentProfile> byName(String name) {
        return ((root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;
            return criteriaBuilder.like(root.get("name"), "%" + name + "%");
        });
    }
    public static Specification<EquipmentProfile> byModel(String model) {
        return ((root, query, criteriaBuilder) -> {
            if (model == null || model.isEmpty()) return null;
            return criteriaBuilder.like(root.get("model"), "%" + model + "%");
        });
    }
    public static Specification<EquipmentProfile> byType(EquipmentProfile.EquipmentType type) {
        return ((root, query, criteriaBuilder) -> {
            if (type.equals(EquipmentProfile.EquipmentType.EQUIPMENT_TYPE_UNSPECIFIED)) return null;
            return criteriaBuilder.equal(root.get("type"), type);
        });
    }
    public static Specification<EquipmentProfile> byStatus(EquipmentProfile.EquipmentStatus status) {
        return ((root, query, criteriaBuilder) -> {
            if (status.equals(EquipmentProfile.EquipmentStatus.EQUIPMENT_STATUS_UNSPECIFIED)) return null;
            return criteriaBuilder.equal(root.get("equipmentStatus"), status);
        });
    }
    public static Specification<EquipmentProfile> byMaxPrice(Integer maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) return null;
            return cb.lessThanOrEqualTo(root.get("pricePerHourCents"), maxPrice);
        };
    }
}