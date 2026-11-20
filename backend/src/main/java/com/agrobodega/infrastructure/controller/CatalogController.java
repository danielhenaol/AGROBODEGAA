package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.service.CatalogService;
import com.agrobodega.domain.entity.NotificationTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogs")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/messages/{code}")
    public ResponseEntity<Map<String, String>> getMessage(@PathVariable String code) {
        return ResponseEntity.ok(Map.of("code", code, "message", catalogService.getMessage(code)));
    }

    @GetMapping("/notifications/{eventCode}")
    public ResponseEntity<Map<String, String>> getNotification(@PathVariable String eventCode) {
        NotificationTemplate t = catalogService.getTemplate(eventCode, "EMAIL");
        return ResponseEntity.ok(Map.of(
                "eventCode", eventCode,
                "subject", t.getSubject(),
                "bodyTemplate", t.getBodyTemplate()));
    }
}
