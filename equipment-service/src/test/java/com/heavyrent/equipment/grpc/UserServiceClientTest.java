package com.heavyrent.equipment.grpc;

import com.heavyrent.grpc.user.GetUserByKeycloakIdRequest;
import com.heavyrent.grpc.user.UserGrpcResponse;
import com.heavyrent.grpc.user.UserGrpcServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceClientTest {

    private UserServiceClient userServiceClient;
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub stub;

    @BeforeEach
    void setUp() {
        userServiceClient = new UserServiceClient();
        stub = mock(UserGrpcServiceGrpc.UserGrpcServiceBlockingStub.class);
        ReflectionTestUtils.setField(userServiceClient, "stub", stub);
    }

    @Test
    void getPublicIdBy_shouldReturnUuid_whenUserServiceReturnsValidResponse() {
        UUID keycloakId = UUID.randomUUID();
        UUID publicId = UUID.randomUUID();

        UserGrpcResponse response = UserGrpcResponse.newBuilder()
                .setPublicId(publicId.toString())
                .build();

        when(stub.getUserByKeycloakId(any(GetUserByKeycloakIdRequest.class))).thenReturn(response);

        UUID result = userServiceClient.getPublicIdBy(keycloakId);

        assertEquals(publicId, result);

        ArgumentCaptor<GetUserByKeycloakIdRequest> captor =
                ArgumentCaptor.forClass(GetUserByKeycloakIdRequest.class);
        verify(stub).getUserByKeycloakId(captor.capture());

        assertEquals(keycloakId.toString(), captor.getValue().getKeycloakId());
    }

    @Test
    void getPublicIdBy_shouldThrowRuntimeException_whenGrpcCallFails() {
        UUID keycloakId = UUID.randomUUID();

        StatusRuntimeException grpcException = Status.NOT_FOUND
                .withDescription("User not found")
                .asRuntimeException();

        when(stub.getUserByKeycloakId(any(GetUserByKeycloakIdRequest.class)))
                .thenThrow(grpcException);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userServiceClient.getPublicIdBy(keycloakId)
        );

        assertSame(grpcException, exception.getCause());
    }

    @Test
    void getPublicIdBy_shouldThrowIllegalArgumentException_whenResponseContainsInvalidUuid() {
        UUID keycloakId = UUID.randomUUID();

        UserGrpcResponse response = UserGrpcResponse.newBuilder()
                .setPublicId("invalid-uuid")
                .build();

        when(stub.getUserByKeycloakId(any(GetUserByKeycloakIdRequest.class))).thenReturn(response);

        assertThrows(IllegalArgumentException.class,
                () -> userServiceClient.getPublicIdBy(keycloakId));
    }
}