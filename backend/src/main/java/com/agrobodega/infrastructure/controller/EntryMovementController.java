package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.response.EntryMovementResponseDTO;
import com.agrobodega.application.dto.request.EntryMovementRequestDTO;
import com.agrobodega.application.service.InventoryService;
import com.agrobodega.domain.entity.EntryMovement;
import com.agrobodega.infrastructure.repository.EntryMovementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entries")
@CrossOrigin(origins = "*")
public class EntryMovementController {

    private final InventoryService inventoryService;
    private final EntryMovementRepository entryMovementRepository;

    public EntryMovementController(InventoryService inventoryService,
                                   EntryMovementRepository entryMovementRepository) {
        this.inventoryService = inventoryService;
        this.entryMovementRepository = entryMovementRepository;
    }

    @GetMapping
    public ResponseEntity<List<EntryMovement>> getAllEntries() {
        return ResponseEntity.ok(entryMovementRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<EntryMovementResponseDTO> registerEntry(@RequestBody Map<String, Object> body) {
        EntryMovementRequestDTO request = new EntryMovementRequestDTO();
        request.setFarmerId(UUID.fromString(body.get("farmerId").toString()));
        request.setProductBaseId(UUID.fromString(body.get("productBaseId").toString()));
        request.setClassificationId(UUID.fromString(body.get("classificationId").toString()));
        request.setLotNumber(Integer.valueOf(body.get("lotNumber").toString()));
        request.setQuantity(new BigDecimal(body.get("quantity").toString()));
        request.setPricePerUnit(new BigDecimal(body.get("pricePerUnit").toString()));
        request.setEntryDate(LocalDate.parse(body.get("entryDate").toString()));
        EntryMovementResponseDTO response = inventoryService.registerEntry(request);
        return ResponseEntity.ok(response);
    }
}
