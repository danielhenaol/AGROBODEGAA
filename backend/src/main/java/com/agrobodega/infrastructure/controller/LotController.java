package com.agrobodega.infrastructure.controller;

import com.agrobodega.domain.entity.Lot;
import com.agrobodega.infrastructure.repository.LotRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inventario de lotes. Los lotes NO se crean a mano: nacen (o crecen)
 * cuando se registra una entrada, y se descuentan con las salidas.
 */
@RestController
@RequestMapping("/api/v1/lots")
@CrossOrigin(origins = "*")
public class LotController {

    private final LotRepository lotRepository;

    public LotController(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    /** GET /api/v1/lots?available=true devuelve solo los lotes con existencias. */
    @GetMapping
    public ResponseEntity<List<Lot>> getLots(@RequestParam(defaultValue = "false") boolean available) {
        return ResponseEntity.ok(available
                ? lotRepository.findByQuantityGreaterThanOrderByEntryDateDesc(BigDecimal.ZERO)
                : lotRepository.findAll());
    }
}
