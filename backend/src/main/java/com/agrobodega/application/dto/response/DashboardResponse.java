package com.agrobodega.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Resumen operativo de la bodega (lo ven el administrador y el propietario). */
public record DashboardResponse(
        long stockBultos,
        long lotsWithStock,
        long farmers,
        long traders,
        long products,
        DayTotals todayEntries,
        DayTotals todayExits,
        int lowStockThreshold,
        List<StockItem> stockByProduct,
        List<StockItem> lowStock,
        List<DayPoint> last7Days,
        List<Activity> recent) {

    /** Movimientos del dia: cuantos registros y cuantos bultos. */
    public record DayTotals(long movements, long bultos) {
    }

    public record StockItem(String product, long bultos, boolean low) {
    }

    public record DayPoint(LocalDate date, long entries, long exits) {
    }

    /** Ultimo movimiento: type es ENTRY o EXIT. */
    public record Activity(String type, Instant occurredAt, LocalDate date, String party,
                           String detail, long bultos) {
    }
}
