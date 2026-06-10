package com.agrobodega.infrastructure.controller;

import com.agrobodega.application.service.StrapiCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/strapi/catalogs")
public class StrapiCatalogController {

    private final StrapiCatalogService strapiCatalogService;

    public StrapiCatalogController(StrapiCatalogService strapiCatalogService) {
        this.strapiCatalogService = strapiCatalogService;
    }

    @GetMapping("/messages/{code}")
    public ResponseEntity<String> getMessageByCode(@PathVariable String code) {
        return ResponseEntity.ok(strapiCatalogService.getMessageByCode(code));
    }

    @GetMapping("/notifications/{eventCode}/subject")
    public ResponseEntity<String> getNotificationSubject(@PathVariable String eventCode) {
        return ResponseEntity.ok(strapiCatalogService.getNotificationSubject(eventCode));
    }

    @GetMapping("/notifications/{eventCode}/body")
    public ResponseEntity<String> getNotificationBody(@PathVariable String eventCode) {
        return ResponseEntity.ok(strapiCatalogService.getNotificationBodyTemplate(eventCode));
    }
}