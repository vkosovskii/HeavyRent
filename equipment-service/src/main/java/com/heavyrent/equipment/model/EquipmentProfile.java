package com.heavyrent.equipment.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "equipment_profiles")
@NoArgsConstructor
public class EquipmentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    @Column(unique = true, nullable = false)
    private String registrationNumber;
    private String brand;
    private String model;
    private long pricePerHourCents;
    private long yearOfManufacture;
    private long ownerId;
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
        deliveryType = DeliveryType.NOT_SPECIFIED;
        equipmentStatus = EquipmentStatus.UNVERIFIED;
        type = EquipmentType.OTHER;
        hasAccreditation = false;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        //TODO don't forget to remove 0d
        latitude = 0d;
        longitude = 0d;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DeliveryType {
        SELF_PROPELLED, TRAILER_NEEDED, DELIVERY_AVAILABLE, NOT_SPECIFIED
    }

    public enum EquipmentStatus {
        FREE, ON_SITE, MAINTENANCE, UNAVAILABLE, UNVERIFIED
    }

    public enum EquipmentType {
        EXCAVATOR,
        CRANE,
        TRUCK,
        BULLDOZER,
        LOADER,
        COMPACTOR,
        CONCRETE_MIXER,
        GENERATOR,
        OTHER
    }

}
