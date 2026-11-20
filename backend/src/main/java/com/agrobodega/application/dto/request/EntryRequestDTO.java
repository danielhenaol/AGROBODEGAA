package com.agrobodega.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Una entrada completa: un cosechero, un numero de lote, una fecha y varias lineas de producto. */
public class EntryRequestDTO {

    @NotNull(message = "El cosechero es obligatorio")
    private UUID farmerId;

    @NotNull(message = "El número de lote es obligatorio")
    @Min(value = 1, message = "El lote debe estar entre 1 y 10")
    @Max(value = 10, message = "El lote debe estar entre 1 y 10")
    private Integer lotNumber;

    /** Fecha y hora de la entrada. Si no viene, se usa el momento actual. */
    private Instant occurredAt;

    /** Solo por compatibilidad: fecha sin hora. */
    private LocalDate entryDate;

    @NotEmpty(message = "La entrada debe tener al menos un producto")
    @Valid
    private List<EntryItemRequestDTO> items;

    public EntryRequestDTO() {}

    public UUID getFarmerId() { return farmerId; }
    public void setFarmerId(UUID farmerId) { this.farmerId = farmerId; }

    public Integer getLotNumber() { return lotNumber; }
    public void setLotNumber(Integer lotNumber) { this.lotNumber = lotNumber; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public List<EntryItemRequestDTO> getItems() { return items; }
    public void setItems(List<EntryItemRequestDTO> items) { this.items = items; }
}
