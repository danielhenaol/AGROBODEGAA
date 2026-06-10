package com.agrobodega.domain.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "product_classification")
public class ProductClassification implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "product_base_id", nullable = false)
    private ProductBase productBase;

    public ProductClassification() {}

    public ProductClassification(UUID id, String name, ProductBase productBase) {
        this.id = id;
        this.name = name;
        this.productBase = productBase;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ProductBase getProductBase() { return productBase; }
    public void setProductBase(ProductBase productBase) { this.productBase = productBase; }
}