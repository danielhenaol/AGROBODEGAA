package com.agrobodega.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "entry_movement")
public class EntryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @ManyToOne
    @JoinColumn(name = "classification_id", nullable = false)
    private ProductClassification classification;

    @Column(nullable = false)
    private Integer lotNumber;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDate entryDate;

    /** Todas las lineas de una misma entrada comparten este identificador. Nulo en registros antiguos. */
    private UUID entryGroupId;

    private LocalDateTime createdAt;

    /** Momento exacto (fecha y hora) del movimiento. Nulo en registros antiguos, que solo tienen fecha. */
    private Instant occurredAt;

    public EntryMovement() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Farmer getFarmer() { return farmer; }
    public void setFarmer(Farmer farmer) { this.farmer = farmer; }

    public ProductClassification getClassification() { return classification; }
    public void setClassification(ProductClassification classification) { this.classification = classification; }

    public Integer getLotNumber() { return lotNumber; }
    public void setLotNumber(Integer lotNumber) { this.lotNumber = lotNumber; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public UUID getEntryGroupId() { return entryGroupId; }
    public void setEntryGroupId(UUID entryGroupId) { this.entryGroupId = entryGroupId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
}