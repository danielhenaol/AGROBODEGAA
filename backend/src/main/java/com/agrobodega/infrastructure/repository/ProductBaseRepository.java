package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.ProductBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductBaseRepository extends JpaRepository<ProductBase, UUID> {
}