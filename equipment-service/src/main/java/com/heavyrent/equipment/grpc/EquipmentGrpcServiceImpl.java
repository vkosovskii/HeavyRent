package com.heavyrent.equipment.grpc;

import com.google.protobuf.Timestamp;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.service.EquipmentProfileService;
import com.heavyrent.grpc.equipment.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;

@GrpcService
public class EquipmentGrpcServiceImpl extends EquipmentGrpcServiceGrpc.EquipmentGrpcServiceImplBase {

    EquipmentProfileService equipmentProfileService;

    public EquipmentGrpcServiceImpl(EquipmentProfileService equipmentProfileService) {
        this.equipmentProfileService = equipmentProfileService;
    }

    @Override
    public void getEquipmentById(GetEquipmentByIdRequest request, StreamObserver<EquipmentGrpcResponse> responseObserver) {
        try {
            long equipmentId = request.getEquipmentId();
            EquipmentGrpcResponse equipmentResponse = toGrpcResponse(equipmentProfileService.findByEquipmentId(equipmentId));
            responseObserver.onNext(equipmentResponse);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Equipment not found with id: " + request.getEquipmentId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getEquipmentsByOwnerId(GetEquipmentsByOwnerIdRequest request, StreamObserver<EquipmentListResponse> responseObserver) {
        try {
            long ownerId = request.getOwnerId();
            List<EquipmentProfileResponse> equipments = equipmentProfileService.findByOwnerId(ownerId);
            EquipmentListResponse.Builder builder = EquipmentListResponse.newBuilder();

            equipments.forEach(equipmentProfileResponse ->
                    builder.addEquipment(toGrpcResponse(equipmentProfileResponse))
            );

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Equipment not found with id: " + request.getOwnerId())
                    .asRuntimeException());
        }
    }

    private EquipmentGrpcResponse toGrpcResponse(EquipmentProfileResponse profile) {
        EquipmentGrpcResponse.Builder response = EquipmentGrpcResponse.newBuilder()
                .setEquipmentId(profile.equipmentId())
                .setRegistrationNumber(profile.registrationNumber())
                .setName(profile.name())
                .setType(EquipmentType.valueOf(profile.type().name()))
                .setBrand(profile.brand())
                .setModel(profile.model())
                .setPricePerHourCents(profile.pricePerHourCents())
                .setYearOfManufacture(profile.yearOfManufacture())
                .setOwnerId(profile.ownerId())
                .setHasOperator(profile.hasOperator())
                .setHasAccreditation(profile.hasAccreditation())
                .setDeliveryType(DeliveryType.valueOf(profile.deliveryType().name()))
                .setEquipmentStatus(EquipmentStatus.valueOf(profile.equipmentStatus().name()))
                .setLatitude(profile.latitude())
                .setLongitude(profile.longitude())
                .setCreatedAt(toTimestamp(profile.createdAt()))
                .setUpdatedAt(toTimestamp(profile.updatedAt()));

        if (profile.availableFrom() != null) {
            response.setAvailableFrom(toTimestamp(profile.availableFrom()));
        }

        return response.build();
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return Timestamp.newBuilder()
                .setSeconds(dateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(dateTime.getNano())
                .build();
    }
}
