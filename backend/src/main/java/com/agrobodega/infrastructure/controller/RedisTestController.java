package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.service.RedisCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/redis")
public class RedisTestController {

    private final RedisCacheService redisCacheService;

    public RedisTestController(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveValue() {
        redisCacheService.save("agrobodega:test", "Redis funcionando correctamente", 10);
        return ResponseEntity.ok("Valor guardado en Redis");
    }

    @GetMapping("/get")
    public ResponseEntity<String> getValue() {
        String value = redisCacheService.get("agrobodega:test");

        if (value == null) {
            return ResponseEntity.ok("No hay valor en Redis");
        }

        return ResponseEntity.ok(value);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteValue() {
        redisCacheService.delete("agrobodega:test");
        return ResponseEntity.ok("Valor eliminado de Redis");
    }
}