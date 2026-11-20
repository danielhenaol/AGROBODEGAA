package com.agrobodega.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Pide al servicio de notificaciones (notification-service) que envie los
 * correos. La plantilla y el envio los resuelve ese servicio; aqui solo se
 * indica el evento, el destinatario y los datos variables.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final CatalogService catalogService;
    private final RestClient client;
    private final String internalToken;

    public NotificationService(CatalogService catalogService,
                               @Value("${notification.service.url:http://localhost:8082}") String baseUrl,
                               @Value("${notification.service.token:}") String internalToken) {
        this.catalogService = catalogService;
        this.internalToken = internalToken;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public void sendEntryNotification(String farmerName, String productName,
                                      String classification, int lotNumber,
                                      double quantity, double totalPrice) {
        Map<String, Object> payload;
        try {
            payload = Map.of(
                    "eventCode", "ENTRY_REGISTERED",
                    "channel", "EMAIL",
                    "to", catalogService.getParameter("NOTIFICATION_DEFAULT_RECIPIENT"),
                    "variables", Map.of(
                            "farmerName", farmerName,
                            "productName", productName,
                            "classification", classification,
                            "lotNumber", String.format("%02d", lotNumber),
                            "quantity", String.format("%.0f", quantity),
                            "totalPrice", String.format("%.2f", totalPrice)));
        } catch (Exception e) {
            log.error("No se pudo preparar la notificacion ENTRY_REGISTERED: {}", e.getMessage());
            return;
        }

        // En segundo plano: una notificacion lenta o fallida no debe frenar ni cancelar el registro.
        CompletableFuture.runAsync(() -> {
            try {
                client.post()
                        .uri("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Internal-Token", internalToken)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Notificacion ENTRY_REGISTERED solicitada");
            } catch (Exception e) {
                log.error("Fallo al solicitar la notificacion ENTRY_REGISTERED: {}", e.getMessage());
            }
        });
    }
}
