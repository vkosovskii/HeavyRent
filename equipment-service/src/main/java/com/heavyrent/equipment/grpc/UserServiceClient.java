package com.heavyrent.equipment.grpc;


import com.heavyrent.grpc.common.UserContextClientInterceptor;
import com.heavyrent.grpc.user.GetUserByKeycloakIdRequest;
import com.heavyrent.grpc.user.UserGrpcResponse;
import com.heavyrent.grpc.user.UserGrpcServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserServiceClient {

    @GrpcClient(value = "user-service", interceptors = {UserContextClientInterceptor.class})
    UserGrpcServiceGrpc.UserGrpcServiceBlockingStub stub;

    public UUID getPublicIdBy(UUID keycloakId) {
        log.info("Get public id by {}", keycloakId);
        GetUserByKeycloakIdRequest request = GetUserByKeycloakIdRequest.newBuilder()
                .setKeycloakId(String.valueOf(keycloakId))
                .build();
        UserGrpcResponse response;
        try {
            response = stub.getUserByKeycloakId(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus(), e);
            throw new IllegalStateException("Failed to fetch user public id", e);
        }
        return UUID.fromString(response.getPublicId());
    }
}
