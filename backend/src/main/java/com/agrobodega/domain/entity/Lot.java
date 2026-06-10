package com.agrobodega.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lot")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_base_id", nullable = false)
    private ProductBase productBase;

    @ManyToOne
    @JoinColumn(name = "classification_id", nullable = false)
    private ProductClassification classification;

    @ManyToOne
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private LocalDate entryDate;

    public Lot() {}

    public Lot(UUID id, ProductBase productBase, ProductClassification classification,
               Farmer farmer, BigDecimal quantity, LocalDate entryDate) {
        this.id = id;
        this.productBase = productBase;
        this.classification = classification;
        this.farmer = farmer;
        this.quantity = quantity;
        this.entryDate = entryDate;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ProductBase getProductBase() { return productBase; }
    public void setProductBase(ProductBase productBase) { this.productBase = productBase; }

    public ProductClassification getClassification() { return classification; }
    public void setClassification(ProductClassification classification) { this.classification = classification; }

    public Farmer getFarmer() { return farmer; }
    public void setFarmer(Farmer farmer) { this.farmer = farmer; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
}