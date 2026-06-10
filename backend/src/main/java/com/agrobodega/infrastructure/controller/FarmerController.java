package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.request.FarmerRequestDTO;
import com.agrobodega.domain.entity.Farmer;
import com.agrobodega.infrastructure.repository.FarmerRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farmers")
@CrossOrigin(origins = "*")
public class FarmerController {

    private final FarmerRepository farmerRepository;

    public FarmerController(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @GetMapping
    public ResponseEntity<List<Farmer>> getAllFarmers() {
        return ResponseEntity.ok(farmerRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Farmer> createFarmer(@Valid @RequestBody FarmerRequestDTO dto) {
        Farmer farmer = new Farmer();
        farmer.setName(dto.getName());
        farmer.setLastName(dto.getLastName());
        farmer.setPhone(dto.getPhone());
        return ResponseEntity.ok(farmerRepository.save(farmer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Farmer> updateFarmer(@PathVariable UUID id, @Valid @RequestBody FarmerRequestDTO dto) {
        return farmerRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setLastName(dto.getLastName());
            existing.setPhone(dto.getPhone());
            return ResponseEntity.ok(farmerRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarmer(@PathVariable UUID id) {
        farmerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}