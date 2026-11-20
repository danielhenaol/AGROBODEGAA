package com.agrobodega.application.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/** Datos para registrar un gasto de la bodega. */
public record ExpenseRequestDTO(
        @NotBlank(message = "La categoría es obligatoria")
        String categoryCode,

        @Size(max = 200, message = "El detalle no puede superar 200 caracteres")
        String description,

        @NotNull(message = "El valor es obligatorio")
        @Positive(message = "El valor debe ser mayor a 0")
        @Digits(integer = 12, fraction = 2, message = "El valor no es válido")
        BigDecimal amount,

        /** Momento del gasto. Si no viene, se usa el momento actual. */
        Instant occurredAt,

        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String registeredBy) {
}
