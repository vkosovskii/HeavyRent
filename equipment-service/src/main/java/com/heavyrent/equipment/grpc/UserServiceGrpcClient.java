package com.heavyrent.equipment.grpc;

import com.heavyrent.grpc.user.GetUserByEmailRequest;
import com.heavyrent.grpc.user.UserGrpcResponse;
import com.heavyrent.grpc.user.UserGrpcServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceGrpcClient {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub blockingStub;

    public UserGrpcResponse getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        GetUserByEmailRequest request = GetUserByEmailRequest.newBuilder().setEmail(email).build();
        UserGrpcResponse response;
        try {
            response = blockingStub.getUserByEmail(request);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
            return null;
        }
        log.info("Greeting: User ID {}", response.getId());
        return response;
    }
}
