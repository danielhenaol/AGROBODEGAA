package com.agrobodega.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Pide al servicio de notificaciones (notification-service) que envie al
 * dueño de la bodega el correo de cada entrada y salida. La plantilla y el envio los resuelve ese servicio; aqui solo se
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

    /** Avisa al dueño de la bodega que se registro UNA entrada (con todas sus lineas en un solo correo). */
    public void sendEntryNotification(String farmerName, int lotNumber, String entryDate,
                                      List<String> itemLines, double totalQuantity, double totalPrice) {
        notifyOwner("ENTRY_REGISTERED", Map.of(
                "farmerName", farmerName,
                "lotNumber", String.format("%02d", lotNumber),
                "entryDate", entryDate,
                "items", String.join("\n", itemLines),
                "totalQuantity", String.format("%.0f", totalQuantity),
                "totalPrice", String.format("%.2f", totalPrice)));
    }

    /** Avisa al dueño de la bodega que se registro UNA salida (con todas sus lineas en un solo correo). */
    public void sendExitNotification(String traderName, String exitDate, List<String> itemLines,
                                     double totalQuantity, double totalPrice) {
        notifyOwner("EXIT_REGISTERED", Map.of(
                "traderName", traderName,
                "exitDate", exitDate,
                "items", String.join("\n", itemLines),
                "totalQuantity", String.format("%.0f", totalQuantity),
                "totalPrice", String.format("%.2f", totalPrice)));
    }

    /** Avisa al dueño de la bodega que se registro un gasto. */
    public void sendExpenseNotification(String category, String description, double amount,
                                        String occurredAt, String registeredBy) {
        notifyOwner("EXPENSE_REGISTERED", Map.of(
                "category", category,
                "description", description,
                "amount", String.format("%.2f", amount),
                "occurredAt", occurredAt,
                "registeredBy", registeredBy));
    }

    private void notifyOwner(String eventCode, Map<String, String> variables) {
        Map<String, Object> payload;
        try {
            payload = Map.of(
                    "eventCode", eventCode,
                    "channel", "EMAIL",
                    "to", catalogService.getParameter("OWNER_EMAIL"),
                    "variables", variables);
        } catch (Exception e) {
            log.error("No se pudo preparar la notificacion {}: {}", eventCode, e.getMessage());
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
                log.info("Notificacion {} solicitada", eventCode);
            } catch (Exception e) {
                log.error("Fallo al solicitar la notificacion {}: {}", eventCode, e.getMessage());
            }
        });
    }
}
