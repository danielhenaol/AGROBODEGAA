package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByExpenseDateBetweenOrderByOccurredAtDesc(LocalDate from, LocalDate to);
}
