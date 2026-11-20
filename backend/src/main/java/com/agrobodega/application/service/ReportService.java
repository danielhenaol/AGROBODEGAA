package com.agrobodega.application.service;

import com.agrobodega.application.dto.response.ReportSummaryResponse;
import com.agrobodega.domain.entity.EntryCharge;
import com.agrobodega.domain.entity.EntryMovement;
import com.agrobodega.domain.entity.Expense;
import com.agrobodega.domain.entity.ExitCharge;
import com.agrobodega.domain.entity.ExitMovement;
import com.agrobodega.infrastructure.exception.BusinessException;
import com.agrobodega.infrastructure.repository.EntryChargeRepository;
import com.agrobodega.infrastructure.repository.EntryMovementRepository;
import com.agrobodega.infrastructure.repository.ExitChargeRepository;
import com.agrobodega.infrastructure.repository.ExitMovementRepository;
import com.agrobodega.infrastructure.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reporte economico del propietario.
 * Ingreso = comisiones cobradas en entradas y salidas. Utilidad = ingreso - gastos.
 */
@Service
public class ReportService {

    private static final int TOP_LIMIT = 8;
    private static final long MAX_DAYS = 366;

    private final EntryMovementRepository entryMovementRepository;
    private final ExitMovementRepository exitMovementRepository;
    private final EntryChargeRepository entryChargeRepository;
    private final ExitChargeRepository exitChargeRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final TimeService timeService;

    public ReportService(EntryMovementRepository entryMovementRepository,
                         ExitMovementRepository exitMovementRepository,
                         EntryChargeRepository entryChargeRepository,
                         ExitChargeRepository exitChargeRepository,
                         ExpenseRepository expenseRepository,
                         ExpenseService expenseService,
                         TimeService timeService) {
        this.entryMovementRepository = entryMovementRepository;
        this.exitMovementRepository = exitMovementRepository;
        this.entryChargeRepository = entryChargeRepository;
        this.exitChargeRepository = exitChargeRepository;
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
        this.timeService = timeService;
    }

    /** Acumulador de bultos y valor para los rankings. */
    private static final class Acc {
        long bultos;
        BigDecimal value = BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(LocalDate fromParam, LocalDate toParam) {
        LocalDate today = timeService.today();
        LocalDate from = fromParam != null ? fromParam : today.withDayOfMonth(1);
        LocalDate to = toParam != null ? toParam : today;
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > MAX_DAYS) {
            throw new BusinessException("INVALID_DATE_RANGE");
        }

        // Comisiones cobradas, por movimiento
        Map<UUID, BigDecimal> entryCommissions = new HashMap<>();
        for (EntryCharge charge : entryChargeRepository.findAll()) {
            entryCommissions.merge(charge.getEntryMovement().getId(), charge.getAmount(), BigDecimal::add);
        }
        Map<UUID, BigDecimal> exitCommissions = new HashMap<>();
        for (ExitCharge charge : exitChargeRepository.findAll()) {
            exitCommissions.merge(charge.getExitMovement().getId(), charge.getAmount(), BigDecimal::add);
        }

        Map<LocalDate, BigDecimal> incomeByDay = new HashMap<>();
        Map<LocalDate, BigDecimal> expensesByDay = new HashMap<>();

        // --- Entradas del periodo ---
        Set<UUID> entryReceipts = new HashSet<>();
        long entryBultos = 0;
        BigDecimal entryValue = BigDecimal.ZERO;
        BigDecimal entryCommission = BigDecimal.ZERO;
        Map<String, Acc> farmers = new LinkedHashMap<>();
        for (EntryMovement m : entryMovementRepository.findAll()) {
            if (!inRange(m.getEntryDate(), from, to)) {
                continue;
            }
            entryReceipts.add(m.getEntryGroupId() != null ? m.getEntryGroupId() : m.getId());
            entryBultos += m.getQuantity().longValue();
            entryValue = entryValue.add(m.getTotalPrice());
            BigDecimal commission = entryCommissions.getOrDefault(m.getId(), BigDecimal.ZERO);
            entryCommission = entryCommission.add(commission);
            incomeByDay.merge(m.getEntryDate(), commission, BigDecimal::add);

            String farmer = m.getFarmer().getName() + " " + m.getFarmer().getLastName();
            Acc acc = farmers.computeIfAbsent(farmer, k -> new Acc());
            acc.bultos += m.getQuantity().longValue();
            acc.value = acc.value.add(m.getTotalPrice());
        }

        // --- Salidas del periodo ---
        Set<UUID> exitReceipts = new HashSet<>();
        long exitBultos = 0;
        BigDecimal exitValue = BigDecimal.ZERO;
        BigDecimal exitCommission = BigDecimal.ZERO;
        Map<String, Acc> products = new LinkedHashMap<>();
        Map<String, Acc> traders = new LinkedHashMap<>();
        for (ExitMovement m : exitMovementRepository.findAll()) {
            if (!inRange(m.getExitDate(), from, to)) {
                continue;
            }
            exitReceipts.add(m.getExitGroupId() != null ? m.getExitGroupId() : m.getId());
            exitBultos += m.getQuantity().longValue();
            exitValue = exitValue.add(m.getTotalPrice());
            BigDecimal commission = exitCommissions.getOrDefault(m.getId(), BigDecimal.ZERO);
            exitCommission = exitCommission.add(commission);
            incomeByDay.merge(m.getExitDate(), commission, BigDecimal::add);

            String product = m.getLot().getProductBase().getName();
            Acc productAcc = products.computeIfAbsent(product, k -> new Acc());
            productAcc.bultos += m.getQuantity().longValue();
            productAcc.value = productAcc.value.add(m.getTotalPrice());

            String trader = m.getTrader().getName() + " " + m.getTrader().getLastName();
            Acc traderAcc = traders.computeIfAbsent(trader, k -> new Acc());
            traderAcc.bultos += m.getQuantity().longValue();
            traderAcc.value = traderAcc.value.add(m.getTotalPrice());
        }

        // --- Gastos del periodo ---
        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByOccurredAtDesc(from, to);
        BigDecimal expensesTotal = BigDecimal.ZERO;
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Expense e : expenses) {
            expensesTotal = expensesTotal.add(e.getAmount());
            expensesByDay.merge(e.getExpenseDate(), e.getAmount(), BigDecimal::add);
            byCategory.merge(e.getCategoryCode(), e.getAmount(), BigDecimal::add);
        }
        Map<String, String> categoryNames = expenseService.categoryNames();
        List<ReportSummaryResponse.CategoryAmount> expensesByCategory = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> item : byCategory.entrySet()) {
            expensesByCategory.add(new ReportSummaryResponse.CategoryAmount(
                    item.getKey(),
                    categoryNames.getOrDefault(item.getKey(), item.getKey()),
                    money(item.getValue())));
        }
        expensesByCategory.sort(Comparator.comparing(ReportSummaryResponse.CategoryAmount::amount).reversed());

        // --- Serie diaria ---
        List<ReportSummaryResponse.DayFinance> daily = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            BigDecimal dayIncome = incomeByDay.getOrDefault(day, BigDecimal.ZERO);
            BigDecimal dayExpenses = expensesByDay.getOrDefault(day, BigDecimal.ZERO);
            daily.add(new ReportSummaryResponse.DayFinance(day, money(dayIncome), money(dayExpenses),
                    money(dayIncome.subtract(dayExpenses))));
        }

        BigDecimal income = entryCommission.add(exitCommission);

        return new ReportSummaryResponse(
                from,
                to,
                new ReportSummaryResponse.Totals(entryReceipts.size(), entryBultos, money(entryValue), money(entryCommission)),
                new ReportSummaryResponse.Totals(exitReceipts.size(), exitBultos, money(exitValue), money(exitCommission)),
                money(income),
                money(expensesTotal),
                money(income.subtract(expensesTotal)),
                expenses.size(),
                daily,
                ranking(products),
                ranking(farmers),
                ranking(traders),
                expensesByCategory);
    }

    private static boolean inRange(LocalDate date, LocalDate from, LocalDate to) {
        return date != null && !date.isBefore(from) && !date.isAfter(to);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Los mayores por valor movido. */
    private static List<ReportSummaryResponse.RankItem> ranking(Map<String, Acc> source) {
        List<ReportSummaryResponse.RankItem> items = new ArrayList<>();
        for (Map.Entry<String, Acc> item : source.entrySet()) {
            items.add(new ReportSummaryResponse.RankItem(
                    item.getKey(), item.getValue().bultos, money(item.getValue().value)));
        }
        items.sort(Comparator.comparing(ReportSummaryResponse.RankItem::value).reversed());
        return items.size() > TOP_LIMIT ? new ArrayList<>(items.subList(0, TOP_LIMIT)) : items;
    }
}
