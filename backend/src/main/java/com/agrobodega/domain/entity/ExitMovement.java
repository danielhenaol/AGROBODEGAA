package com.agrobodega.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exit_movement")
public class ExitMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @ManyToOne
    @JoinColumn(name = "trader_id", nullable = false)
    private Trader trader;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDate exitDate;

    /** Todas las lineas de una misma salida comparten este identificador. Nulo en registros antiguos. */
    private UUID exitGroupId;

    private LocalDateTime createdAt;

    /** Momento exacto (fecha y hora) del movimiento. Nulo en registros antiguos, que solo tienen fecha. */
    private Instant occurredAt;

    public ExitMovement() {}

    public ExitMovement(UUID id, Lot lot, Trader trader, BigDecimal quantity,
                        BigDecimal pricePerUnit, BigDecimal totalPrice, LocalDate exitDate) {
        this.id = id;
        this.lot = lot;
        this.trader = trader;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalPrice = totalPrice;
        this.exitDate = exitDate;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Lot getLot() { return lot; }
    public void setLot(Lot lot) { this.lot = lot; }

    public Trader getTrader() { return trader; }
    public void setTrader(Trader trader) { this.trader = trader; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public UUID getExitGroupId() { return exitGroupId; }
    public void setExitGroupId(UUID exitGroupId) { this.exitGroupId = exitGroupId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }
}