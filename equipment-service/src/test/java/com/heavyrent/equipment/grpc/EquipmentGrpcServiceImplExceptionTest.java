package com.heavyrent.equipment.grpc;

import com.heavyrent.equipment.base.BaseTest;
import com.heavyrent.equipment.service.EquipmentProfileService;
import com.heavyrent.grpc.common.UserContextHolder;
import com.heavyrent.grpc.equipment.EquipmentCreateRequest;
import com.heavyrent.grpc.equipment.EquipmentGrpcResponse;
import com.heavyrent.grpc.equipment.EquipmentListResponse;
import com.heavyrent.grpc.equipment.GetEquipmentByIdRequest;
import com.heavyrent.grpc.equipment.ListEquipmentRequest;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentGrpcServiceImplExceptionTest extends BaseTest {

    @Mock
    private EquipmentProfileService service;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private StreamObserver<EquipmentGrpcResponse> equipmentResponseObserver;

    @Mock
    private StreamObserver<EquipmentListResponse> listResponseObserver;

    @InjectMocks
    private EquipmentGrpcServiceImpl grpcService;

    @Test
    void getEquipmentById_shouldThrowIllegalArgumentException_whenUuidIsInvalid() {
        GetEquipmentByIdRequest request = getEquipmentByIdRequestWithInvalidId();

        assertThrows(
                IllegalArgumentException.class,
                () -> grpcService.getEquipmentById(request, equipmentResponseObserver)
        );

        verifyNoInteractions(service);
        verifyNoInteractions(equipmentResponseObserver);
    }

    @Test
    void getEquipmentById_shouldPropagateNoSuchElementException_whenServiceThrows() {
        GetEquipmentByIdRequest request = getEquipmentByIdRequest();

        when(service.findByEquipmentId(equipmentPublicId))
                .thenThrow(new NoSuchElementException("Equipment not found: " + equipmentPublicId));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> grpcService.getEquipmentById(request, equipmentResponseObserver)
        );

        assertEquals("Equipment not found: " + equipmentPublicId, exception.getMessage());
        verify(service).findByEquipmentId(equipmentPublicId);
        verifyNoInteractions(equipmentResponseObserver);
    }

    @Test
    void createEquipment_shouldThrowPermissionDenied_whenRoleIsNotOwner() {
        EquipmentCreateRequest request = getEquipmentCreateRequest();
        Context context = Context.current().withValue(UserContextHolder.KEY, customerContext());

        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> context.run(() -> grpcService.createEquipment(request, equipmentResponseObserver))
        );

        assertEquals(Status.PERMISSION_DENIED.getCode(), exception.getStatus().getCode());
        assertEquals("Wrong ROLE: CUSTOMER", exception.getStatus().getDescription());

        verifyNoInteractions(service);
        verifyNoInteractions(userServiceClient);
        verifyNoInteractions(equipmentResponseObserver);
    }

    @Test
    void createEquipment_shouldThrowIllegalArgumentException_whenContextKeycloakIdIsInvalid() {
        EquipmentCreateRequest request = getEquipmentCreateRequest();
        Context context = Context.current().withValue(UserContextHolder.KEY, ownerContextWithInvalidKeycloakId());

        assertThrows(
                IllegalArgumentException.class,
                () -> context.run(() -> grpcService.createEquipment(request, equipmentResponseObserver))
        );

        verifyNoInteractions(service);
        verifyNoInteractions(userServiceClient);
        verifyNoInteractions(equipmentResponseObserver);
    }

    @Test
    void createEquipment_shouldPropagateRuntimeException_whenUserServiceClientFails() {
        EquipmentCreateRequest request = getEquipmentCreateRequest();
        Context context = Context.current().withValue(UserContextHolder.KEY, ownerContext());

        when(userServiceClient.getPublicIdBy(equipmentId))
                .thenThrow(new RuntimeException("User service unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> context.run(() -> grpcService.createEquipment(request, equipmentResponseObserver))
        );

        assertEquals("User service unavailable", exception.getMessage());

        verify(userServiceClient).getPublicIdBy(equipmentId);
        verifyNoInteractions(service);
        verifyNoInteractions(equipmentResponseObserver);
    }

    @Test
    void createEquipment_shouldPropagateNoSuchElementException_whenServiceThrows() {
        EquipmentCreateRequest request = getEquipmentCreateRequest();
        Context context = Context.current().withValue(UserContextHolder.KEY, ownerContext());

        when(userServiceClient.getPublicIdBy(equipmentId)).thenReturn(ownerPublicId);
        when(service.createEquipmentProfile(any(), eq(equipmentId), eq(ownerPublicId)))
                .thenThrow(new NoSuchElementException("Owner not found"));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> context.run(() -> grpcService.createEquipment(request, equipmentResponseObserver))
        );

        assertEquals("Owner not found", exception.getMessage());

        verify(userServiceClient).getPublicIdBy(equipmentId);
        verify(service).createEquipmentProfile(any(), eq(equipmentId), eq(ownerPublicId));
        verifyNoInteractions(equipmentResponseObserver);
    }

    @Test
    void getListEquipment_shouldThrowIllegalArgumentException_whenOwnerIdIsInvalid() {
        ListEquipmentRequest request = getListEquipmentRequestWithInvalidOwnerId();

        assertThrows(
                IllegalArgumentException.class,
                () -> grpcService.getListEquipment(request, listResponseObserver)
        );

        verifyNoInteractions(service);
        verifyNoInteractions(listResponseObserver);
    }

    @Test
    void getListEquipment_shouldPropagateIllegalArgumentException_whenServiceRejectsPaging() {
        ListEquipmentRequest request = getListEquipmentRequestWithNegativePage();

        when(service.findAll(any(), eq(-1), eq(10)))
                .thenThrow(new IllegalArgumentException("Page index must not be less than zero"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> grpcService.getListEquipment(request, listResponseObserver)
        );

        assertEquals("Page index must not be less than zero", exception.getMessage());
        verify(service).findAll(any(), eq(-1), eq(10));
        verifyNoInteractions(listResponseObserver);
    }

    @Test
    void getListEquipment_shouldPropagateNoSuchElementException_whenServiceThrows() {
        ListEquipmentRequest request = getListEquipmentRequestWithoutFilters();

        when(service.findAll(any(), eq(0), eq(10)))
                .thenThrow(new NoSuchElementException("Equipment not found with filters"));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> grpcService.getListEquipment(request, listResponseObserver)
        );

        assertEquals("Equipment not found with filters", exception.getMessage());
        verify(service).findAll(any(), eq(0), eq(10));
        verifyNoInteractions(listResponseObserver);
    }

    @Test
    void getListEquipment_shouldPropagateRuntimeException_whenUnexpectedErrorOccurs() {
        ListEquipmentRequest request = getListEquipmentRequestWithoutFilters();

        when(service.findAll(any(), eq(0), eq(10)))
                .thenThrow(new RuntimeException("Database unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> grpcService.getListEquipment(request, listResponseObserver)
        );

        assertEquals("Database unavailable", exception.getMessage());
        verify(service).findAll(any(), eq(0), eq(10));
        verifyNoInteractions(listResponseObserver);
    }

    @Test
    void getListEquipment_shouldReturnEmptyResponse_whenServiceReturnsEmptyPage() {
        ListEquipmentRequest request = getListEquipmentRequestWithoutFilters();

        when(service.findAll(any(), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(List.of()));

        assertDoesNotThrow(() -> grpcService.getListEquipment(request, listResponseObserver));

        verify(service).findAll(any(), eq(0), eq(10));
        verify(listResponseObserver).onNext(any());
        verify(listResponseObserver).onCompleted();
    }
}