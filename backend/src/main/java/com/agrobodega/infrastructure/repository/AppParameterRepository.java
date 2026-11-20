package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.AppParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppParameterRepository extends JpaRepository<AppParameter, Long> {

    Optional<AppParameter> findByParamKeyAndActiveTrue(String paramKey);
}
