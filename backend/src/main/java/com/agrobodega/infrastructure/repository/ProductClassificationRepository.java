package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.ProductClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductClassificationRepository extends JpaRepository<ProductClassification, UUID> {
    List<ProductClassification> findByProductBaseId(UUID productBaseId);
}