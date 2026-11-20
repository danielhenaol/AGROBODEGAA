package com.agrobodega.application.service;

import com.agrobodega.application.dto.request.ExpenseRequestDTO;
import com.agrobodega.application.dto.response.ExpenseResponse;
import com.agrobodega.domain.entity.Expense;
import com.agrobodega.domain.entity.ExpenseCategory;
import com.agrobodega.infrastructure.exception.BusinessException;
import com.agrobodega.infrastructure.repository.ExpenseCategoryRepository;
import com.agrobodega.infrastructure.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Gastos de la bodega: los registran el administrador y el propietario; el propietario recibe un correo. */
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final TimeService timeService;
    private final NotificationService notificationService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseCategoryRepository categoryRepository,
                          TimeService timeService,
                          NotificationService notificationService) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.timeService = timeService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategory> categories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @Transactional
    public ExpenseResponse register(ExpenseRequestDTO request) {
        ExpenseCategory category = categoryRepository.findByCodeAndActiveTrue(request.categoryCode())
                .orElseThrow(() -> new BusinessException("EXPENSE_CATEGORY_NOT_FOUND"));

        Instant occurredAt = timeService.resolve(request.occurredAt(), null);

        Expense expense = new Expense();
        expense.setCategoryCode(category.getCode());
        expense.setDescription(blankToNull(request.description()));
        expense.setAmount(request.amount());
        expense.setOccurredAt(occurredAt);
        expense.setExpenseDate(timeService.toLocalDate(occurredAt));
        expense.setRegisteredBy(blankToNull(request.registeredBy()));
        expense.setCreatedAt(Instant.now());
        expense = expenseRepository.save(expense);

        notificationService.sendExpenseNotification(
                category.getName(),
                expense.getDescription() == null ? "—" : expense.getDescription(),
                expense.getAmount().doubleValue(),
                timeService.format(occurredAt),
                expense.getRegisteredBy() == null ? "—" : expense.getRegisteredBy());

        return toResponse(expense, category.getName());
    }

    /** Gastos entre dos fechas (incluidas). Si no se indican, el mes en curso. */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate from, LocalDate to) {
        LocalDate today = timeService.today();
        LocalDate start = from != null ? from : today.withDayOfMonth(1);
        LocalDate end = to != null ? to : today;

        Map<String, String> names = categoryNames();
        return expenseRepository.findByExpenseDateBetweenOrderByOccurredAtDesc(start, end).stream()
                .map(e -> toResponse(e, names.getOrDefault(e.getCategoryCode(), e.getCategoryCode())))
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("EXPENSE_NOT_FOUND"));
        expenseRepository.delete(expense);
    }

    /** Codigo de categoria -> nombre (incluye categorias desactivadas, para gastos antiguos). */
    public Map<String, String> categoryNames() {
        Map<String, String> names = new HashMap<>();
        for (ExpenseCategory c : categoryRepository.findAll()) {
            names.put(c.getCode(), c.getName());
        }
        return names;
    }

    private ExpenseResponse toResponse(Expense e, String categoryName) {
        return new ExpenseResponse(e.getId(), e.getCategoryCode(), categoryName, e.getDescription(),
                e.getAmount(), e.getOccurredAt(), e.getExpenseDate(), e.getRegisteredBy());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
