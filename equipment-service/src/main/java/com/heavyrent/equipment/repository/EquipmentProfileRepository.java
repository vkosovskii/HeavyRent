package com.heavyrent.equipment.repository;

import com.heavyrent.equipment.model.EquipmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentProfileRepository extends JpaRepository<EquipmentProfile, Long>, JpaSpecificationExecutor<EquipmentProfile> {

    List<EquipmentProfile> findByOwnerId(UUID ownerId);
    Optional<EquipmentProfile> findEquipmentProfileById(UUID publicId);
    void deleteByPublicId(UUID publicId);
}
