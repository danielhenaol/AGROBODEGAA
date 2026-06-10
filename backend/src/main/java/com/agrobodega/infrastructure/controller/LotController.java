package com.agrobodega.infrastructure.controller;

import com.agrobodega.domain.entity.Farmer;
import com.agrobodega.domain.entity.Lot;
import com.agrobodega.domain.entity.ProductBase;
import com.agrobodega.domain.entity.ProductClassification;
import com.agrobodega.infrastructure.repository.FarmerRepository;
import com.agrobodega.infrastructure.repository.LotRepository;
import com.agrobodega.infrastructure.repository.ProductBaseRepository;
import com.agrobodega.infrastructure.repository.ProductClassificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lots")
@CrossOrigin(origins = "*")
public class LotController {

    private final LotRepository lotRepository;
    private final FarmerRepository farmerRepository;
    private final ProductBaseRepository productBaseRepository;
    private final ProductClassificationRepository classificationRepository;

    public LotController(LotRepository lotRepository,
                         FarmerRepository farmerRepository,
                         ProductBaseRepository productBaseRepository,
                         ProductClassificationRepository classificationRepository) {
        this.lotRepository = lotRepository;
        this.farmerRepository = farmerRepository;
        this.productBaseRepository = productBaseRepository;
        this.classificationRepository = classificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Lot>> getAllLots() {
        return ResponseEntity.ok(lotRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Lot> createLot(@RequestBody Map<String, Object> body) {
        Farmer farmer = farmerRepository.findById(UUID.fromString(body.get("farmerId").toString()))
                .orElseThrow(() -> new RuntimeException("Cosechero no encontrado"));
        ProductBase product = productBaseRepository.findById(UUID.fromString(body.get("productBaseId").toString()))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        ProductClassification classification = classificationRepository.findById(UUID.fromString(body.get("classificationId").toString()))
                .orElseThrow(() -> new RuntimeException("Clasificación no encontrada"));

        Lot lot = new Lot();
        lot.setFarmer(farmer);
        lot.setProductBase(product);
        lot.setClassification(classification);
        lot.setQuantity(new java.math.BigDecimal(body.get("quantity").toString()));
        lot.setEntryDate(LocalDate.parse(body.get("entryDate").toString()));

        return ResponseEntity.ok(lotRepository.save(lot));
    }
}