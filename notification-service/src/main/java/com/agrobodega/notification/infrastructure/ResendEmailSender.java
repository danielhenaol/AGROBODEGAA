package com.agrobodega.notification.infrastructure;

import com.agrobodega.notification.domain.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Envia correos con la API de Resend. Se activa cuando existe RESEND_API_KEY. */
@Component
@ConditionalOnExpression("!'${resend.api-key:}'.isBlank()")
public class ResendEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);

    private final RestClient client;
    private final String from;

    public ResendEmailSender(@Value("${resend.api-key}") String apiKey,
                             @Value("${resend.from:onboarding@resend.dev}") String from) {
        this.from = from;
        this.client = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void send(String to, String subject, String body) {
        client.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", from,
                        "to", List.of(to),
                        "subject", subject,
                        "text", body))
                .retrieve()
                .toBodilessEntity();
        log.info("Correo enviado a {} ({})", to, subject);
    }
}
