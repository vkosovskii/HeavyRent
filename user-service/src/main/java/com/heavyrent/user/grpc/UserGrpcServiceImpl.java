package com.heavyrent.user.grpc;

import com.heavyrent.grpc.user.*;
import com.heavyrent.user.dto.UserProfileResponse;
import com.heavyrent.user.service.UserProfileService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    UserProfileService service;

    public UserGrpcServiceImpl(UserProfileService service) {
        this.service = service;
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserGrpcResponse> responseObserver) {
        long id = request.getId();
        UserGrpcResponse user = toGrpcResponse(service.getUserById(id));
        responseObserver.onNext(user);
        responseObserver.onCompleted();
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

