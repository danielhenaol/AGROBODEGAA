package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.request.ExitRequestDTO;
import com.agrobodega.application.dto.response.ExitReceiptResponseDTO;
import com.agrobodega.application.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exits")
@CrossOrigin(origins = "*")
public class ExitMovementController {

    private final InventoryService inventoryService;

    public ExitMovementController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** Salidas agrupadas: cada elemento es una salida con todas sus lineas. */
    @GetMapping
    public ResponseEntity<List<ExitReceiptResponseDTO>> getExits() {
        return ResponseEntity.ok(inventoryService.getExits());
    }

    /** Registra una salida completa (varias lineas) en una sola operacion. */
    @PostMapping
    public ResponseEntity<ExitReceiptResponseDTO> registerExit(@Valid @RequestBody ExitRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.registerExit(request));
    }
}
