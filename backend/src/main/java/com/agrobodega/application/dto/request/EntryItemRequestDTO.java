package com.agrobodega.application.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

/** Una linea de la entrada: un producto, su clasificacion, los bultos y el precio por bulto. */
public class EntryItemRequestDTO {

    @NotNull(message = "El producto es obligatorio")
    private UUID productBaseId;

    @NotNull(message = "La clasificación es obligatoria")
    private UUID classificationId;

    @NotNull(message = "Los bultos son obligatorios")
    @Positive(message = "Los bultos deben ser mayores a 0")
    @Digits(integer = 9, fraction = 0, message = "Los bultos deben ser un número entero")
    private BigDecimal quantity;

    @NotNull(message = "El precio por bulto es obligatorio")
    @Positive(message = "El precio por bulto debe ser mayor a 0")
    private BigDecimal pricePerUnit;

    public EntryItemRequestDTO() {}

    public UUID getProductBaseId() { return productBaseId; }
    public void setProductBaseId(UUID productBaseId) { this.productBaseId = productBaseId; }

    public UUID getClassificationId() { return classificationId; }
    public void setClassificationId(UUID classificationId) { this.classificationId = classificationId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
}
