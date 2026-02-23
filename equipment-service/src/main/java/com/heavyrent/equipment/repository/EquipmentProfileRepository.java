package com.heavyrent.equipment.repository;

import com.heavyrent.equipment.model.EquipmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentProfileRepository extends JpaRepository<EquipmentProfile, Long> {

    List<EquipmentProfile> findByOwnerId(long ownerId);

    Optional<EquipmentProfile> findEquipmentProfileById(long id);
}
