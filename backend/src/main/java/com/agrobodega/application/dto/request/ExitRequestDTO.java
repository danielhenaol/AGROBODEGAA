package com.agrobodega.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Una salida completa: un negociante, una fecha y varias lineas (cada una de un lote). */
public class ExitRequestDTO {

    @NotNull(message = "El negociante es obligatorio")
    private UUID traderId;

    /** Fecha y hora de la salida. Si no viene, se usa el momento actual. */
    private Instant occurredAt;

    /** Solo por compatibilidad: fecha sin hora. */
    private LocalDate exitDate;

    @NotEmpty(message = "La salida debe tener al menos un producto")
    @Valid
    private List<ExitItemRequestDTO> items;

    public ExitRequestDTO() {}

    public UUID getTraderId() { return traderId; }
    public void setTraderId(UUID traderId) { this.traderId = traderId; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }

    public List<ExitItemRequestDTO> getItems() { return items; }
    public void setItems(List<ExitItemRequestDTO> items) { this.items = items; }
}
