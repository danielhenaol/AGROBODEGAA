package com.agrobodega.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class EntryMovementRequestDTO {

    @NotNull
    private UUID productBaseId;

    @NotNull
    private UUID classificationId;

    @NotNull
    private UUID farmerId;

    @NotNull
    @Min(1) @Max(10)
    private Integer lotNumber;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotNull
    @Positive
    private BigDecimal pricePerUnit;

    @NotNull
    private LocalDate entryDate;

    public EntryMovementRequestDTO() {}

    public UUID getProductBaseId() { return productBaseId; }
    public void setProductBaseId(UUID productBaseId) { this.productBaseId = productBaseId; }

    public UUID getClassificationId() { return classificationId; }
    public void setClassificationId(UUID classificationId) { this.classificationId = classificationId; }

    public UUID getFarmerId() { return farmerId; }
    public void setFarmerId(UUID farmerId) { this.farmerId = farmerId; }

    public Integer getLotNumber() { return lotNumber; }
    public void setLotNumber(Integer lotNumber) { this.lotNumber = lotNumber; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
}