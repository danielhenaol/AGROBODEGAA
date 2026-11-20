package com.agrobodega.application.service;

import com.agrobodega.infrastructure.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Manejo de fechas con hora. Todo se guarda como Instant (UTC) y el "dia" de un
 * movimiento se calcula en la zona horaria de la bodega (parametro APP_TIMEZONE).
 */
@Service
public class TimeService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter HUMAN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CatalogService catalogService;

    public TimeService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public ZoneId zone() {
        try {
            return ZoneId.of(catalogService.getParameter("APP_TIMEZONE"));
        } catch (Exception e) {
            return DEFAULT_ZONE;
        }
    }

    public LocalDate today() {
        return LocalDate.now(zone());
    }

    public LocalDate toLocalDate(Instant instant) {
        return instant.atZone(zone()).toLocalDate();
    }

    /** Texto para correos, en la hora de la bodega: 20/11/2026 16:20. */
    public String format(Instant instant) {
        return HUMAN.format(instant.atZone(zone()));
    }

    /**
     * Decide el momento del movimiento: usa el indicado; si no viene, usa la fecha antigua
     * (a medianoche) o, en ultimo caso, el momento actual. No acepta momentos futuros.
     */
    public Instant resolve(Instant occurredAt, LocalDate legacyDate) {
        if (occurredAt != null) {
            if (occurredAt.isAfter(Instant.now().plus(Duration.ofMinutes(10)))) {
                throw new BusinessException("DATE_IN_FUTURE");
            }
            return occurredAt;
        }
        if (legacyDate != null) {
            return legacyDate.atStartOfDay(zone()).toInstant();
        }
        return Instant.now();
    }

    /** Momento para ordenar movimientos antiguos que solo tienen fecha. */
    public Instant instantOf(Instant occurredAt, LocalDate date) {
        if (occurredAt != null) {
            return occurredAt;
        }
        return date.atStartOfDay(zone()).toInstant();
    }
}
