package com.agrobodega.domain.entity;

import jakarta.persistence.*;

/** Categoria de gasto (catalogo guardado en la base de datos). */
@Entity
@Table(name = "expense_category")
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean active;

    public ExpenseCategory() {}

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Integer getSortOrder() { return sortOrder; }
    public boolean isActive() { return active; }
}
