package com.agrobodega.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Gasto de la bodega (nomina, servicios, transporte, etc.). */
@Entity
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Codigo de la categoria (tabla expense_category). */
    @Column(nullable = false, length = 50)
    private String categoryCode;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    /** Momento exacto en que ocurrio el gasto. */
    @Column(nullable = false)
    private Instant occurredAt;

    /** Dia del gasto en la zona horaria de la bodega (para agrupar en los reportes). */
    @Column(nullable = false)
    private LocalDate expenseDate;

    /** Correo de quien lo registro (informativo). */
    @Column(length = 150)
    private String registeredBy;

    @Column(nullable = false)
    private Instant createdAt;

    public Expense() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(String registeredBy) { this.registeredBy = registeredBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
