package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.EntryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EntryMovementRepository extends JpaRepository<EntryMovement, UUID> {
}