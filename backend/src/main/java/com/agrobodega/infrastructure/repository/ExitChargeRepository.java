package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.ExitCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExitChargeRepository extends JpaRepository<ExitCharge, UUID> {
    Optional<ExitCharge> findByExitMovementId(UUID exitMovementId);
}