package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.request.TraderRequestDTO;
import com.agrobodega.domain.entity.Trader;
import com.agrobodega.infrastructure.repository.TraderRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/traders")
@CrossOrigin(origins = "*")
public class TraderController {

    private final TraderRepository traderRepository;

    public TraderController(TraderRepository traderRepository) {
        this.traderRepository = traderRepository;
    }

    @GetMapping
    public ResponseEntity<List<Trader>> getAllTraders() {
        return ResponseEntity.ok(traderRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Trader> createTrader(@Valid @RequestBody TraderRequestDTO dto) {
        Trader trader = new Trader();
        trader.setName(dto.getName());
        trader.setLastName(dto.getLastName());
        trader.setPhone(dto.getPhone());
        return ResponseEntity.ok(traderRepository.save(trader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trader> updateTrader(@PathVariable UUID id, @Valid @RequestBody TraderRequestDTO dto) {
        return traderRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setLastName(dto.getLastName());
            existing.setPhone(dto.getPhone());
            return ResponseEntity.ok(traderRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrader(@PathVariable UUID id) {
        traderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}