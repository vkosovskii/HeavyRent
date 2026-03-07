package com.heavyrent.equipment.mapper;

import com.google.protobuf.Timestamp;
import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.grpc.equipment.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class EquipmentMapper {

    public static EquipmentProfile toEntity(EquipmentProfileRequest equipmentProfileResponse) {
        return fillIn(equipmentProfileResponse, new EquipmentProfile());
    }

    public static EquipmentProfile toEntity(EquipmentProfileRequest equipmentProfileResponse, EquipmentProfile profile) {
        return fillIn(equipmentProfileResponse, profile);
    }

    private static EquipmentProfile fillIn(EquipmentProfileRequest equipmentProfileResponse, EquipmentProfile profile) {
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

    public static EquipmentProfileResponse toResponse(EquipmentProfile equipmentProfile) {
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

    public static EquipmentProfileRequest toRequest(EquipmentCreateRequest request) {
        return EquipmentProfileRequest.builder()
                .name(request.getName())
                .type(EquipmentProfile.EquipmentType.valueOf(request.getType().name()))
                .registrationNumber(request.getRegistrationNumber())
                .brand(request.getBrand())
                .model(request.getModel())
                .pricePerHourCents(request.getPricePerHourCents())
                .yearOfManufacture(request.getYearOfManufacture())
                .hasOperator(request.getHasOperator())
                .hasAccreditation(request.getHasAccreditation())
                .deliveryType(EquipmentProfile.DeliveryType.valueOf(request.getDeliveryType().name()))
                .equipmentStatus(EquipmentProfile.EquipmentStatus.valueOf(request.getEquipmentStatus().name()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

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
                .setDeliveryType(DeliveryType.valueOf(response.deliveryType().name()))
                .setEquipmentStatus(EquipmentStatus.valueOf(response.equipmentStatus().name()))
                .setLatitude(response.latitude())
                .setLongitude(response.longitude())
                .setCreatedAt(toTimestamp(response.createdAt()))
                .setUpdatedAt(toTimestamp(response.updatedAt()))
                .build();
    }

    public static EquipmentProfile.EquipmentType toEntityType(EquipmentType protoType) {
        return EquipmentProfile.EquipmentType.valueOf(protoType.name());
    }

    public static EquipmentProfile.EquipmentStatus toEntityStatus(EquipmentStatus protoStatus) {
        return EquipmentProfile.EquipmentStatus.valueOf(protoStatus.name());
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
