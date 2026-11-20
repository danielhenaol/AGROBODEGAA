package com.agrobodega.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Una salida ya registrada, con todas sus lineas agrupadas. */
public class ExitReceiptResponseDTO {

    private UUID id;
    private String traderName;
    private LocalDate exitDate;
    private Instant occurredAt;
    private List<Item> items;
    private BigDecimal totalQuantity;
    private BigDecimal totalPrice;
    private BigDecimal totalCommission;

    public static class Item {
        private UUID movementId;
        private UUID lotId;
        private Integer lotNumber;
        private String productName;
        private String classificationName;
        private String farmerName;
        private BigDecimal quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal totalPrice;
        private BigDecimal commissionAmount;

        public UUID getMovementId() { return movementId; }
        public void setMovementId(UUID movementId) { this.movementId = movementId; }
        public UUID getLotId() { return lotId; }
        public void setLotId(UUID lotId) { this.lotId = lotId; }
        public Integer getLotNumber() { return lotNumber; }
        public void setLotNumber(Integer lotNumber) { this.lotNumber = lotNumber; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getClassificationName() { return classificationName; }
        public void setClassificationName(String classificationName) { this.classificationName = classificationName; }
        public String getFarmerName() { return farmerName; }
        public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public BigDecimal getCommissionAmount() { return commissionAmount; }
        public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTraderName() { return traderName; }
    public void setTraderName(String traderName) { this.traderName = traderName; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getTotalCommission() { return totalCommission; }
    public void setTotalCommission(BigDecimal totalCommission) { this.totalCommission = totalCommission; }
}
