package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.request.ExpenseRequestDTO;
import com.agrobodega.application.dto.response.ExpenseResponse;
import com.agrobodega.application.service.ExpenseService;
import com.agrobodega.domain.entity.ExpenseCategory;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /** Categorias de gasto (catalogo en la base de datos). */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, String>>> categories() {
        List<ExpenseCategory> categories = expenseService.categories();
        return ResponseEntity.ok(categories.stream()
                .map(c -> Map.of("code", c.getCode(), "name", c.getName()))
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(expenseService.list(from, to));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> register(@Valid @RequestBody ExpenseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.register(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
