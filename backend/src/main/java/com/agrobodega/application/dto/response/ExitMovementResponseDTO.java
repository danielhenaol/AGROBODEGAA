package com.agrobodega.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ExitMovementResponseDTO {

    private UUID exitMovementId;
    private UUID lotId;
    private String productName;
    private String classificationName;
    private String traderName;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;
    private BigDecimal commissionAmount;
    private BigDecimal remainingLotQuantity;
    private LocalDate exitDate;

    public ExitMovementResponseDTO() {}

    public UUID getExitMovementId() { return exitMovementId; }
    public void setExitMovementId(UUID exitMovementId) { this.exitMovementId = exitMovementId; }

    public UUID getLotId() { return lotId; }
    public void setLotId(UUID lotId) { this.lotId = lotId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getClassificationName() { return classificationName; }
    public void setClassificationName(String classificationName) { this.classificationName = classificationName; }

    public String getTraderName() { return traderName; }
    public void setTraderName(String traderName) { this.traderName = traderName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

    public BigDecimal getRemainingLotQuantity() { return remainingLotQuantity; }
    public void setRemainingLotQuantity(BigDecimal remainingLotQuantity) { this.remainingLotQuantity = remainingLotQuantity; }

    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }
}