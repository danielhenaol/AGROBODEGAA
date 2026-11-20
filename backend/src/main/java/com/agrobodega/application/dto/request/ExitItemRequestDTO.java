package com.agrobodega.application.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

/** Una linea de la salida: de que lote sale, cuantos bultos y a que precio por bulto. */
public class ExitItemRequestDTO {

    @NotNull(message = "El lote es obligatorio")
    private UUID lotId;

    @NotNull(message = "Los bultos son obligatorios")
    @Positive(message = "Los bultos deben ser mayores a 0")
    @Digits(integer = 9, fraction = 0, message = "Los bultos deben ser un número entero")
    private BigDecimal quantity;

    @NotNull(message = "El precio por bulto es obligatorio")
    @Positive(message = "El precio por bulto debe ser mayor a 0")
    private BigDecimal pricePerUnit;

    public ExitItemRequestDTO() {}

    public UUID getLotId() { return lotId; }
    public void setLotId(UUID lotId) { this.lotId = lotId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
}
