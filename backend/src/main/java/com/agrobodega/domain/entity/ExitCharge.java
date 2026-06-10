package com.agrobodega.domain.entity;

import com.agrobodega.domain.enums.ChargeType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "exit_charge")
public class ExitCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "exit_movement_id", nullable = false)
    private ExitMovement exitMovement;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeType chargeType;

    public ExitCharge() {}

    public ExitCharge(UUID id, ExitMovement exitMovement,
                      BigDecimal amount, ChargeType chargeType) {
        this.id = id;
        this.exitMovement = exitMovement;
        this.amount = amount;
        this.chargeType = chargeType;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ExitMovement getExitMovement() { return exitMovement; }
    public void setExitMovement(ExitMovement exitMovement) { this.exitMovement = exitMovement; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public ChargeType getChargeType() { return chargeType; }
    public void setChargeType(ChargeType chargeType) { this.chargeType = chargeType; }
}