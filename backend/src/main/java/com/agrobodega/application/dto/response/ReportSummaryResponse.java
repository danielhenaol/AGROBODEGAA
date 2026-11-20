package com.agrobodega.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte economico de un periodo (solo propietario).
 * Ingreso de la bodega = comisiones cobradas en entradas y salidas.
 * Utilidad = ingreso - gastos.
 */
public record ReportSummaryResponse(
        LocalDate from,
        LocalDate to,
        Totals entries,
        Totals exits,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal net,
        int expenseCount,
        List<DayFinance> daily,
        List<RankItem> topProducts,
        List<RankItem> topFarmers,
        List<RankItem> topTraders,
        List<CategoryAmount> expensesByCategory) {

    /** Totales de entradas o de salidas: registros, bultos, valor movido y comision cobrada. */
    public record Totals(long receipts, long bultos, BigDecimal value, BigDecimal commission) {
    }

    public record DayFinance(LocalDate date, BigDecimal income, BigDecimal expenses, BigDecimal net) {
    }

    public record RankItem(String name, long bultos, BigDecimal value) {
    }

    public record CategoryAmount(String code, String name, BigDecimal amount) {
    }
}
