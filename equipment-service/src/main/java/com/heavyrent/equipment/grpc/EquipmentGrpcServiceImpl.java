package com.heavyrent.equipment.grpc;

import com.google.protobuf.Timestamp;
import com.heavyrent.equipment.dto.EquipmentFilterRequest;
import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.equipment.service.EquipmentProfileService;
import com.heavyrent.grpc.common.UserContext;
import com.heavyrent.grpc.common.UserContextHolder;
import com.heavyrent.grpc.equipment.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.UUID;

@GrpcService
public class EquipmentGrpcServiceImpl extends EquipmentGrpcServiceGrpc.EquipmentGrpcServiceImplBase {

    EquipmentProfileService service;
    UserServiceClient userServiceClient;

    public EquipmentGrpcServiceImpl(EquipmentProfileService service, UserServiceClient userServiceClient) {
        this.service = service;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public void getEquipmentById(GetEquipmentByIdRequest request, StreamObserver<EquipmentGrpcResponse> responseObserver) {
        try {
            UUID equipmentId = UUID.fromString(request.getEquipmentId());
            EquipmentProfileResponse serviceResponse = service.findByEquipmentId(equipmentId);
            EquipmentGrpcResponse response = toGrpcResponse(serviceResponse);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Equipment not found with public ID: " + request.getEquipmentId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createEquipment(EquipmentCreateRequest request, StreamObserver<EquipmentGrpcResponse> responseObserver) {
        UserContext context = UserContextHolder.KEY.get();
        try {
            if (!context.role().equals("OWNER")) {
                responseObserver.onError(Status.PERMISSION_DENIED
                        .withDescription("Wrong ROLE to create equipment profile: " + context.role())
                        .asRuntimeException());
                return;
            }
            EquipmentProfileRequest createRequest = toRequest(request);
            UUID ownerKeycloakId = UUID.fromString(context.keycloakId());
            UUID ownerPublicUuid = userServiceClient.getPublicIdBy(ownerKeycloakId);
            EquipmentProfileResponse response = service.createEquipmentProfile(createRequest, ownerKeycloakId, ownerPublicUuid);
            responseObserver.onNext(toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (DataIntegrityViolationException e) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Equipment already exists")
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error creating equipment profile: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getListEquipment(ListEquipmentRequest request, StreamObserver<EquipmentListResponse> responseObserver) {
        try {

            Integer maxPricePerHourCents = request.hasMaxPricePerHourCents() ? request.getMaxPricePerHourCents().getValue() : null;
            UUID ownerPublicId = request.getOwnerId().isEmpty() ? null : UUID.fromString(request.getOwnerId());

            EquipmentFilterRequest filterRequest = EquipmentFilterRequest.builder()
                    .ownerPublicId(ownerPublicId)
                    .name(request.getName())
                    .model(request.getModel())
                    .maxPricePerHourCents(maxPricePerHourCents)
                    .type(EquipmentProfile.EquipmentType.valueOf(request.getType().name()))
                    .equipmentStatus(EquipmentProfile.EquipmentStatus.valueOf(request.getEquipmentStatus().name()))
                    .build();
            EquipmentListResponse.Builder response = EquipmentListResponse.newBuilder();
            Page<EquipmentProfileResponse> equipmentPage = service.findAll(filterRequest, request.getPage(), request.getPageSize());
            equipmentPage.forEach(equipment ->
                response.addEquipment(toGrpcResponse(equipment))
            );
            response.setTotalCount((int) equipmentPage.getTotalElements());
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Equipment not found with filters")
                    .asRuntimeException());
        }
    }

    private EquipmentProfileRequest toRequest(EquipmentCreateRequest request) {
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

    private EquipmentGrpcResponse toGrpcResponse(EquipmentProfileResponse response) {
        return EquipmentGrpcResponse.newBuilder()
                .setName(response.name())
                .setEquipmentId(response.publicId().toString())
                .setType(EquipmentType.valueOf(response.type().name()))
                .setRegistrationNumber(response.registrationNumber())
                .setBrand(response.brand())
                .setModel(response.model())
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
