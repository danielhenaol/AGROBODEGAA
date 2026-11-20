package com.agrobodega.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Una entrada ya registrada, con todas sus lineas agrupadas. */
public class EntryReceiptResponseDTO {

    private UUID id;
    private String farmerName;
    private Integer lotNumber;
    private LocalDate entryDate;
    private Instant occurredAt;
    private List<Item> items;
    private BigDecimal totalQuantity;
    private BigDecimal totalPrice;
    private BigDecimal totalCommission;

    public static class Item {
        private UUID movementId;
        private String productName;
        private String classificationName;
        private BigDecimal quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal totalPrice;
        private BigDecimal commissionAmount;

        public UUID getMovementId() { return movementId; }
        public void setMovementId(UUID movementId) { this.movementId = movementId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getClassificationName() { return classificationName; }
        public void setClassificationName(String classificationName) { this.classificationName = classificationName; }
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
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public Integer getLotNumber() { return lotNumber; }
    public void setLotNumber(Integer lotNumber) { this.lotNumber = lotNumber; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getTotalCommission() { return totalCommission; }
    public void setTotalCommission(BigDecimal totalCommission) { this.totalCommission = totalCommission; }
}
