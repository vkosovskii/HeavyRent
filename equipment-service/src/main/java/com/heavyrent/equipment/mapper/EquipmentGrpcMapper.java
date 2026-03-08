package com.heavyrent.equipment.mapper;

import com.google.protobuf.Timestamp;
import com.heavyrent.equipment.dto.EquipmentFilterRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.grpc.equipment.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static com.heavyrent.equipment.mapper.EquipmentEntityMapper.toEntityStatus;
import static com.heavyrent.equipment.mapper.EquipmentEntityMapper.toEntityType;

public class EquipmentGrpcMapper {

    public static EquipmentGrpcResponse toGrpcResponse(EquipmentProfileResponse response) {
        return EquipmentGrpcResponse.newBuilder()
                .setName(response.name())
                .setEquipmentId(response.publicId().toString())
                .setType(EquipmentType.valueOf(response.type().name()))
                .setRegistrationNumber(response.registrationNumber())
                .setBrand(response.brand())
                .setModel(response.model())
                .setHasOperator(response.hasOperator())
                .setPricePerHourCents(response.pricePerHourCents())
                .setHasAccreditation(response.hasAccreditation())
                .setAvailableFrom(toTimestamp(response.availableFrom()))
                .setYearOfManufacture((int) response.yearOfManufacture())
                .setDeliveryType(DeliveryType.valueOf(response.deliveryType().name()))
                .setEquipmentStatus(EquipmentStatus.valueOf(response.equipmentStatus().name()))
                .setLatitude(response.latitude())
                .setLongitude(response.longitude())
                .setCreatedAt(toTimestamp(response.createdAt()))
                .setUpdatedAt(toTimestamp(response.updatedAt()))
                .build();
    }

    public static EquipmentFilterRequest toEquipmentFilterRequest(ListEquipmentRequest request) {
        Integer maxPricePerHourCents = request.hasMaxPricePerHourCents() ? request.getMaxPricePerHourCents().getValue() : null;
        UUID ownerPublicId = request.getOwnerId().isEmpty() ? null : UUID.fromString(request.getOwnerId());

        return EquipmentFilterRequest.builder()
                .ownerPublicId(ownerPublicId)
                .name(request.getName())
                .model(request.getModel())
                .maxPricePerHourCents(maxPricePerHourCents)
                .type(toEntityType(request.getType()))
                .equipmentStatus(toEntityStatus(request.getEquipmentStatus()))
                .build();
    }

    private static Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return Timestamp.newBuilder()
                .setSeconds(dateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(dateTime.getNano())
                .build();
    }
}
