package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.EntryCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntryChargeRepository extends JpaRepository<EntryCharge, UUID> {
    Optional<EntryCharge> findByEntryMovementId(UUID entryMovementId);
}