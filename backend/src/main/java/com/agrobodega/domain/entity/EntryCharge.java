package com.agrobodega.domain.entity;

import com.agrobodega.domain.enums.ChargeType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "entry_charge")
public class EntryCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "entry_movement_id", nullable = false)
    private EntryMovement entryMovement;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeType chargeType;

    public EntryCharge() {}

    public EntryCharge(UUID id, EntryMovement entryMovement,
                       BigDecimal amount, ChargeType chargeType) {
        this.id = id;
        this.entryMovement = entryMovement;
        this.amount = amount;
        this.chargeType = chargeType;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public EntryMovement getEntryMovement() { return entryMovement; }
    public void setEntryMovement(EntryMovement entryMovement) { this.entryMovement = entryMovement; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public ChargeType getChargeType() { return chargeType; }
    public void setChargeType(ChargeType chargeType) { this.chargeType = chargeType; }
}