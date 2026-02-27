package com.heavyrent.user.grpc;

import com.heavyrent.grpc.user.*;
import com.heavyrent.user.dto.UserProfileResponse;
import com.heavyrent.user.service.UserProfileService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.NoSuchElementException;

@GrpcService
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    UserProfileService profileService;

    public UserGrpcServiceImpl(UserProfileService service) {
        this.profileService = service;
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserGrpcResponse> responseObserver) {
        try {
            long id = request.getId();
            UserGrpcResponse user = toGrpcResponse(profileService.getUserById(id));
            responseObserver.onNext(user);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("User not found with id: " + request.getId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserGrpcResponse> responseObserver) {
        try {
            String email = request.getEmail();
            UserGrpcResponse user = toGrpcResponse(profileService.findUserByEmail(email));
            responseObserver.onNext(user);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("User not found with email: " + request.getEmail())
                    .asRuntimeException());
        }
    }

    private UserGrpcResponse toGrpcResponse(UserProfileResponse user) {
        return UserGrpcResponse.newBuilder()
                .setId(user.id())
                .setEmail(user.email())
                .setRole(UserRole.valueOf(user.role().name()))
                .setStatus(UserStatus.valueOf(user.status().name()))
                .build();
    }
}

