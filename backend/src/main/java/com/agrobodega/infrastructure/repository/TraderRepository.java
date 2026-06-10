package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TraderRepository extends JpaRepository<Trader, UUID> {
}