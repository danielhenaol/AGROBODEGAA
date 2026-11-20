package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findByActiveTrueOrderBySortOrderAsc();

    Optional<ExpenseCategory> findByCodeAndActiveTrue(String code);
}
