package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.ExitMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExitMovementRepository extends JpaRepository<ExitMovement, UUID> {
    List<ExitMovement> findByTraderId(UUID traderId);
    List<ExitMovement> findByLotId(UUID lotId);
}