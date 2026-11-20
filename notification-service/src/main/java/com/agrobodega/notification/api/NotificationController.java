package com.agrobodega.notification.api;

import com.agrobodega.notification.domain.EmailSender;
import com.agrobodega.notification.domain.NotificationTemplate;
import com.agrobodega.notification.domain.TemplateRenderer;
import com.agrobodega.notification.infrastructure.TemplateRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final TemplateRepository templates;
    private final EmailSender emailSender;
    private final String internalToken;

    public NotificationController(TemplateRepository templates,
                                  EmailSender emailSender,
                                  @Value("${internal.token:}") String internalToken) {
        this.templates = templates;
        this.emailSender = emailSender;
        this.internalToken = internalToken;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> send(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @Valid @RequestBody SendNotificationRequest request) {

        // Si INTERNAL_TOKEN esta configurado, solo acepta llamadas que lo traigan.
        if (!internalToken.isBlank() && !internalToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token interno invalido"));
        }

        if (!"EMAIL".equals(request.channelOrDefault())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Canal no soportado: " + request.channelOrDefault()));
        }

        NotificationTemplate template = templates.find(request.eventCode(), request.channelOrDefault())
                .orElse(null);
        if (template == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Plantilla no encontrada: " + request.eventCode()));
        }

        String subject = TemplateRenderer.render(template.subject(), request.variables());
        String body = TemplateRenderer.render(template.bodyTemplate(), request.variables());

        try {
            emailSender.send(request.to(), subject, body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No se pudo enviar la notificacion: " + e.getMessage()));
        }

        return ResponseEntity.accepted().body(Map.of("status", "SENT", "eventCode", request.eventCode()));
    }
}
