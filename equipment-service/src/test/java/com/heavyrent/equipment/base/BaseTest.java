package com.heavyrent.equipment.base;

import com.heavyrent.equipment.dto.EquipmentProfileRequest;
import com.heavyrent.equipment.model.EquipmentProfile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTest {

    public EquipmentProfile getEquipmentProfile() {
        EquipmentProfile equipmentProfile = new EquipmentProfile();
        equipmentProfile.setId(1L);
        equipmentProfile.setRegistrationNumber("А123БВ777");
        equipmentProfile.setName("Экскаватор Caterpillar");
        equipmentProfile.setType(EquipmentProfile.EquipmentType.EXCAVATOR);
        equipmentProfile.setBrand("Caterpillar");
        equipmentProfile.setModel("320D");
        equipmentProfile.setPricePerHourCents(150000L);
        equipmentProfile.setYearOfManufacture(2019L);
        equipmentProfile.setOwnerId(1L);
        equipmentProfile.setHasOperator(true);
        equipmentProfile.setHasAccreditation(true);
        equipmentProfile.setDeliveryType(EquipmentProfile.DeliveryType.SELF_PROPELLED);
        equipmentProfile.setEquipmentStatus(EquipmentProfile.EquipmentStatus.FREE);
        equipmentProfile.setLatitude(0d);
        equipmentProfile.setLongitude(0d);
        equipmentProfile.setCreatedAt(LocalDateTime.now());
        equipmentProfile.setUpdatedAt(LocalDateTime.now());
        return equipmentProfile;
    }

    public List<EquipmentProfile> getEquipmentProfileList() {
        List<EquipmentProfile> list = new ArrayList<>();
        list.add(getEquipmentProfile());
        return list;
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
}
