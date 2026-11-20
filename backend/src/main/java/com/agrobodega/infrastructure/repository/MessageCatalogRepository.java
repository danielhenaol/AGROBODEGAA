package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.MessageCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageCatalogRepository extends JpaRepository<MessageCatalog, Long> {

    Optional<MessageCatalog> findByCodeAndLocaleAndActiveTrue(String code, String locale);
}
