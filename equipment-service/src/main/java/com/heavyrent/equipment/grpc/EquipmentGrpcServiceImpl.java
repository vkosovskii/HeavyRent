package com.heavyrent.equipment.grpc;

import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.dto.EquipmentProfileResponse;
import com.heavyrent.equipment.service.EquipmentProfileService;
import com.heavyrent.grpc.common.UserContext;
import com.heavyrent.grpc.common.UserContextHolder;
import com.heavyrent.grpc.equipment.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;

import java.util.UUID;

import static com.heavyrent.equipment.mapper.EquipmentEntityMapper.*;
import static com.heavyrent.equipment.mapper.EquipmentGrpcMapper.toEquipmentFilterRequest;
import static com.heavyrent.equipment.mapper.EquipmentGrpcMapper.toGrpcResponse;

@Slf4j
@GrpcService
public class EquipmentGrpcServiceImpl extends EquipmentGrpcServiceGrpc.EquipmentGrpcServiceImplBase {

    private final EquipmentProfileService service;
    private final UserServiceClient userServiceClient;

    public EquipmentGrpcServiceImpl(EquipmentProfileService service, UserServiceClient userServiceClient) {
        this.service = service;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public void getEquipmentById(GetEquipmentByIdRequest request, StreamObserver<EquipmentGrpcResponse> responseObserver) {
        log.info("Get equipment by id: {}", request.getEquipmentId());
        UUID equipmentId = UUID.fromString(request.getEquipmentId());
        EquipmentProfileResponse serviceResponse = service.findByEquipmentId(equipmentId);
        responseObserver.onNext(toGrpcResponse(serviceResponse));
        responseObserver.onCompleted();
    }

    @Override
    public void createEquipment(EquipmentCreateRequest request, StreamObserver<EquipmentGrpcResponse> responseObserver) {
        log.info("Creat equipment");
        UserContext context = UserContextHolder.KEY.get();
        isOwner(context.role());
        EquipmentProfileRequest createRequest = toRequest(request);
        UUID ownerKeycloakId = UUID.fromString(context.keycloakId());
        UUID ownerPublicUuid = userServiceClient.getPublicIdBy(ownerKeycloakId);
        EquipmentProfileResponse response = service.createEquipmentProfile(createRequest, ownerKeycloakId, ownerPublicUuid);
        responseObserver.onNext(toGrpcResponse(response));
        responseObserver.onCompleted();
    }

    @Override
    public void getListEquipment(ListEquipmentRequest request, StreamObserver<EquipmentListResponse> responseObserver) {
        log.info("Get list equipment");
        int pageSize = request.getPageSize() == 0 ? 20 : request.getPageSize();
        int page = request.getPage();

        EquipmentListResponse.Builder response = EquipmentListResponse.newBuilder();
        Page<EquipmentProfileResponse> equipmentPage = service.findAll(toEquipmentFilterRequest(request), page, pageSize);
        equipmentPage.forEach(equipment -> response.addEquipment(toGrpcResponse(equipment)));
        response.setTotalCount((int) equipmentPage.getTotalElements());
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private void isOwner(String role) {
        log.info("Check if equipment owner is {}", role);
        if (!role.equals("OWNER")) {
            throw Status.PERMISSION_DENIED
                    .withDescription("Wrong ROLE: " + role)
                    .asRuntimeException();
        }
    }
}
