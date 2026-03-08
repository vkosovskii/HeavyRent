package com.heavyrent.equipment.base;

import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.model.EquipmentProfile;
import com.heavyrent.grpc.common.UserContext;
import com.heavyrent.grpc.equipment.EquipmentCreateRequest;
import com.heavyrent.grpc.equipment.GetEquipmentByIdRequest;
import com.heavyrent.grpc.equipment.ListEquipmentRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseTest {

    public UUID equipmentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public UUID ownerPublicId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public UUID equipmentPublicId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public String invalidUuid = "invalid-uuid";

    public UserContext ownerContext() {
        return new UserContext(equipmentId.toString(), "OWNER");
    }

    public UserContext customerContext() {
        return new UserContext(equipmentId.toString(), "CUSTOMER");
    }

    public UserContext ownerContextWithInvalidKeycloakId() {
        return new UserContext(invalidUuid, "OWNER");
    }

    public EquipmentProfile getEquipmentProfile() {
        EquipmentProfile equipmentProfile = new EquipmentProfile();
        equipmentProfile.setId(1L);
        equipmentProfile.setPublicId(equipmentPublicId);
        equipmentProfile.setOwnerPublicId(ownerPublicId);
        equipmentProfile.setRegistrationNumber("А123БВ777");
        equipmentProfile.setName("Caterpillar");
        equipmentProfile.setType(EquipmentProfile.EquipmentType.EXCAVATOR);
        equipmentProfile.setBrand("Caterpillar");
        equipmentProfile.setModel("320D");
        equipmentProfile.setPricePerHourCents(150000L);
        equipmentProfile.setYearOfManufacture(2019L);
        equipmentProfile.setOwnerKeycloakId(equipmentId);
        equipmentProfile.setHasOperator(true);
        equipmentProfile.setHasAccreditation(true);
        equipmentProfile.setDeliveryType(EquipmentProfile.DeliveryType.SELF_PROPELLED);
        equipmentProfile.setEquipmentStatus(EquipmentProfile.EquipmentStatus.FREE);
        equipmentProfile.setAvailableFrom(LocalDateTime.now().plusDays(1));
        equipmentProfile.setLatitude(0d);
        equipmentProfile.setLongitude(0d);
        equipmentProfile.setCreatedAt(LocalDateTime.now());
        equipmentProfile.setUpdatedAt(LocalDateTime.now());
        return equipmentProfile;
    }

    public EquipmentProfileRequest getEquipmentProfileRequest() {
        return EquipmentProfileRequest.builder()
                .registrationNumber("А123БВ777")
                .name("Экскаватор Caterpillar")
                .type(EquipmentProfile.EquipmentType.EXCAVATOR)
                .brand("Caterpillar")
                .model("320D")
                .pricePerHourCents(150000L)
                .yearOfManufacture(2019L)
                .hasOperator(true)
                .hasAccreditation(true)
                .deliveryType(EquipmentProfile.DeliveryType.SELF_PROPELLED)
                .equipmentStatus(EquipmentProfile.EquipmentStatus.FREE)
                .latitude(0d)
                .longitude(0d)
                .build();
    }

    public GetEquipmentByIdRequest getEquipmentByIdRequest() {
        return GetEquipmentByIdRequest.newBuilder()
                .setEquipmentId(equipmentPublicId.toString())
                .build();
    }

    public GetEquipmentByIdRequest getEquipmentByIdRequestWithInvalidId() {
        return GetEquipmentByIdRequest.newBuilder()
                .setEquipmentId(invalidUuid)
                .build();
    }

    public EquipmentCreateRequest getEquipmentCreateRequest() {
        return EquipmentCreateRequest.newBuilder()
                .setRegistrationNumber("А123БВ777")
                .setName("Экскаватор Caterpillar")
                .setBrand("Caterpillar")
                .setModel("320D")
                .setPricePerHourCents(150000)
                .setYearOfManufacture(2019)
                .setHasOperator(true)
                .setHasAccreditation(true)
                .build();
    }

    public ListEquipmentRequest getListEquipmentRequest() {
        return ListEquipmentRequest.newBuilder()
                .setOwnerId(ownerPublicId.toString())
                .setName("Caterpillar")
                .setModel("320D")
                .setPage(0)
                .setPageSize(10)
                .build();
    }

    public ListEquipmentRequest getListEquipmentRequestWithInvalidOwnerId() {
        return ListEquipmentRequest.newBuilder()
                .setOwnerId(invalidUuid)
                .setPage(0)
                .setPageSize(10)
                .build();
    }

    public ListEquipmentRequest getListEquipmentRequestWithNegativePage() {
        return ListEquipmentRequest.newBuilder()
                .setPage(-1)
                .setPageSize(10)
                .build();
    }

    public ListEquipmentRequest getListEquipmentRequestWithoutFilters() {
        return ListEquipmentRequest.newBuilder()
                .setPage(0)
                .setPageSize(10)
                .build();
    }
}