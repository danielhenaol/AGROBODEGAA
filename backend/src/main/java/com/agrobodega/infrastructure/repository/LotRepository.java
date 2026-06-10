package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LotRepository extends JpaRepository<Lot, UUID> {
    List<Lot> findByFarmerId(UUID farmerId);
    Optional<Lot> findByProductBaseIdAndClassificationIdAndFarmerId(
            UUID productBaseId, UUID classificationId, UUID farmerId);
}