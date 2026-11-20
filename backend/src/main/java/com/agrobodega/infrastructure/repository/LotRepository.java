package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.Lot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LotRepository extends JpaRepository<Lot, UUID> {

    List<Lot> findByFarmerId(UUID farmerId);

    /** Lotes con existencias en bodega. */
    List<Lot> findByQuantityGreaterThanOrderByEntryDateDesc(BigDecimal quantity);

    /** Busca el lote al que corresponde una entrada. */
    Optional<Lot> findByProductBaseIdAndClassificationIdAndFarmerIdAndLotNumber(
            UUID productBaseId, UUID classificationId, UUID farmerId, Integer lotNumber);

    /** Bloquea la fila para que dos salidas simultaneas no descuenten el mismo stock. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lot l where l.id = :id")
    Optional<Lot> findByIdForUpdate(@Param("id") UUID id);
}
