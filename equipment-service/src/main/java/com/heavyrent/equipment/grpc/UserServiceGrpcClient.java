package com.heavyrent.equipment.grpc;

import com.heavyrent.grpc.user.GetUserByPublicIdRequest;
import com.heavyrent.grpc.user.UserGrpcResponse;
import com.heavyrent.grpc.user.UserGrpcServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UserServiceGrpcClient {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub blockingStub;

    public UserGrpcResponse getUserByEmail(UUID userPublicId) {
        log.info("Getting user by user public ID: {}", userPublicId);

        GetUserByPublicIdRequest request = GetUserByPublicIdRequest.newBuilder().setPublicId(userPublicId.toString()).build();
        UserGrpcResponse response;
        try {
            response = blockingStub.getUserByPublicId(request);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
            return null;
        }
        log.info("Greeting: User ID {}", response.getPublicId());
        return response;
    }
}
