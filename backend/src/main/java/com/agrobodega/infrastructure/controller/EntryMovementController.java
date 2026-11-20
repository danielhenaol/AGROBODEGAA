package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.request.EntryRequestDTO;
import com.agrobodega.application.dto.response.EntryReceiptResponseDTO;
import com.agrobodega.application.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entries")
@CrossOrigin(origins = "*")
public class EntryMovementController {

    private final InventoryService inventoryService;

    public EntryMovementController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** Entradas agrupadas: cada elemento es una entrada con todas sus lineas. */
    @GetMapping
    public ResponseEntity<List<EntryReceiptResponseDTO>> getEntries() {
        return ResponseEntity.ok(inventoryService.getEntries());
    }

    /** Registra una entrada completa (varias lineas) en una sola operacion. */
    @PostMapping
    public ResponseEntity<EntryReceiptResponseDTO> registerEntry(@Valid @RequestBody EntryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.registerEntry(request));
    }
}
