package com.heavyrent.equipment.grpc;

import com.heavyrent.equipment.dto.EquipmentFilterRequest;
import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.service.EquipmentProfileService;
import com.heavyrent.grpc.common.UserContext;
import com.heavyrent.grpc.common.UserContextHolder;
import com.heavyrent.grpc.equipment.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;

import java.util.NoSuchElementException;
import java.util.UUID;

import static com.heavyrent.equipment.mapper.EquipmentMapper.*;

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
                    .type(toEntityType(request.getType()))
                    .equipmentStatus(toEntityStatus(request.getEquipmentStatus()))
                    .build();
            EquipmentListResponse.Builder response = EquipmentListResponse.newBuilder();
            Page<EquipmentProfileResponse> equipmentPage = service.findAll(filterRequest, request.getPage(), request.getPageSize());
            equipmentPage.forEach(equipment ->
                    response.addEquipment(toGrpcResponse(equipment))
            );
            response.setTotalCount((int) equipmentPage.getTotalElements());
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid request: " + e.getMessage())
                    .asRuntimeException());
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Equipment not found with filters")
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
}
