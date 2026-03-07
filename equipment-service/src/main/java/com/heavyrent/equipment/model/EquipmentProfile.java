package com.heavyrent.equipment.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "equipment_profiles")
@NoArgsConstructor
public class EquipmentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Column(nullable = false)
    private UUID ownerKeycloakId;
    @Column(nullable = false)
    private UUID ownerPublicId;

    private String name;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    @Column(unique = true, nullable = false)
    private String registrationNumber;
    private String brand;
    private String model;
    private long pricePerHourCents;
    private long yearOfManufacture;

    private boolean hasOperator;
    private boolean hasAccreditation;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus equipmentStatus;

    private LocalDateTime availableFrom;
    private Double latitude;
    private Double longitude;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        publicId = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DeliveryType {
        DELIVERY_TYPE_UNSPECIFIED,
        TRAILER_NEEDED,
        DELIVERY_AVAILABLE,
        SELF_PROPELLED,
        DELIVERY_NOT_DETERMINED
    }

    public enum EquipmentStatus {
        EQUIPMENT_STATUS_UNSPECIFIED,
        ON_SITE,
        MAINTENANCE,
        UNAVAILABLE,
        FREE,
        STATUS_NOT_DETERMINED
    }

    public enum EquipmentType {
        EQUIPMENT_TYPE_UNSPECIFIED,
        CRANE,
        TRUCK,
        BULLDOZER,
        LOADER,
        COMPACTOR,
        CONCRETE_MIXER,
        GENERATOR,
        OTHER,
        EXCAVATOR
    }
}
