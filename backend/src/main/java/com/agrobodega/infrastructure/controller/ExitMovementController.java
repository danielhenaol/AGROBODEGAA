package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.request.ExitMovementRequestDTO;
import com.agrobodega.application.dto.response.ExitMovementResponseDTO;
import com.agrobodega.application.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exits")
public class ExitMovementController {

    private final InventoryService inventoryService;

    public ExitMovementController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<ExitMovementResponseDTO> registerExit(
            @Valid @RequestBody ExitMovementRequestDTO request) {
        ExitMovementResponseDTO response = inventoryService.registerExit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
