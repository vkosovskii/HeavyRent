package com.heavyrent.user.grpc;

import com.google.protobuf.Timestamp;
import com.heavyrent.grpc.common.UserContext;
import com.heavyrent.grpc.common.UserContextHolder;
import com.heavyrent.grpc.user.*;
import com.heavyrent.user.dto.UserProfileResponse;
import com.heavyrent.user.service.UserProfileService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.UUID;

@GrpcService
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private final UserProfileService profileService;

    public UserGrpcServiceImpl(UserProfileService service) {
        this.profileService = service;
    }

    @Override
    public void getUserByPublicId(GetUserByPublicIdRequest request, StreamObserver<UserGrpcResponse> responseObserver) {
        try {
            UUID publicId = UUID.fromString(request.getPublicId());
            UserGrpcResponse response = toGrpcResponse(profileService.getUserByUuid(publicId));
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver
                    .onError(Status.NOT_FOUND
                            .withDescription("User not found with public ID: " + request.getPublicId())
                            .asRuntimeException());
        }
    }

    @Override
    public void getUserByKeycloakId(GetUserByKeycloakIdRequest request, StreamObserver<UserGrpcResponse> responseObserver) {
        try {
            UUID keycloakId = UUID.fromString(request.getKeycloakId());
            UserGrpcResponse response = toGrpcResponse(profileService.getUserByKeycloakId(keycloakId));
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("User not found with keycloak ID: " + request.getKeycloakId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getCurrentUser(GetCurrentUserRequest request, StreamObserver<UserGrpcResponse> responseObserver) {
        try {
            UserContext ctx = UserContextHolder.KEY.get();
            UUID keycloakId = UUID.fromString(ctx.keycloakId());
            UserGrpcResponse response = toGrpcResponse(profileService.getUserByKeycloakId(keycloakId));
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("User not found with keycloak")
                    .asRuntimeException());
        }
    }

    private UserGrpcResponse toGrpcResponse(UserProfileResponse user) {
        return UserGrpcResponse.newBuilder()
                .setPublicId(user.publicId().toString())
                .setEmail(user.email())
                .setFirstName(user.firstName())
                .setLastName(user.lastName())
                .setPhone(user.phone())
                .setIsVerified(user.isVerified())
                .setRole(UserRole.valueOf(user.role().name()))
                .setStatus(UserStatus.valueOf(user.status().name()))
                .setUpdatedAt(toTimestamp(user.updatedAt()))
                .build();
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Timestamp.newBuilder().setSeconds(dateTime.toEpochSecond(ZoneOffset.UTC)).setNanos(dateTime.getNano()).build();
    }
}

