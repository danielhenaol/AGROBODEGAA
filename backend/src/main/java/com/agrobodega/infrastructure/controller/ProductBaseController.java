package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.dto.request.ProductBaseRequestDTO;
import com.agrobodega.domain.entity.ProductBase;
import com.agrobodega.domain.entity.ProductClassification;
import com.agrobodega.infrastructure.repository.ProductBaseRepository;
import com.agrobodega.infrastructure.repository.ProductClassificationRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*")
public class ProductBaseController {

    private final ProductBaseRepository productBaseRepository;
    private final ProductClassificationRepository classificationRepository;

    public ProductBaseController(ProductBaseRepository productBaseRepository,
                                 ProductClassificationRepository classificationRepository) {
        this.productBaseRepository = productBaseRepository;
        this.classificationRepository = classificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductBase>> getAllProducts() {
        return ResponseEntity.ok(productBaseRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<ProductBase> createProduct(@Valid @RequestBody ProductBaseRequestDTO dto) {
        ProductBase product = new ProductBase();
        product.setName(dto.getName());
        return ResponseEntity.ok(productBaseRepository.save(product));
    }

    @PostMapping("/{productId}/classifications")
    public ResponseEntity<ProductClassification> createClassification(
            @PathVariable UUID productId,
            @RequestBody Map<String, String> body) {
        ProductBase product = productBaseRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        ProductClassification classification = new ProductClassification();
        classification.setName(body.get("name"));
        classification.setProductBase(product);
        return ResponseEntity.ok(classificationRepository.save(classification));
    }

    @GetMapping("/{productId}/classifications")
    public ResponseEntity<List<ProductClassification>> getClassifications(@PathVariable UUID productId) {
        return ResponseEntity.ok(classificationRepository.findByProductBaseId(productId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBase> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductBaseRequestDTO dto) {
        return productBaseRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            return ResponseEntity.ok(productBaseRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productBaseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/classifications/{classId}")
    public ResponseEntity<Void> deleteClassification(@PathVariable UUID productId, @PathVariable UUID classId) {
        classificationRepository.deleteById(classId);
        return ResponseEntity.noContent().build();
    }
}