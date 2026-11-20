package com.agrobodega.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String categoryCode,
        String categoryName,
        String description,
        BigDecimal amount,
        Instant occurredAt,
        LocalDate expenseDate,
        String registeredBy) {
}
