package com.agrobodega.application.service;

import com.agrobodega.domain.entity.NotificationTemplate;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Envia notificaciones usando las plantillas del catalogo (base de datos).
 * Por ahora el envio usa SendGrid; en el siguiente paso se reemplaza por
 * el servicio de notificaciones independiente.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final CatalogService catalogService;

    @Value("${sendgrid.api.key:}")
    private String apiKey;

    @Value("${sendgrid.from.email:}")
    private String fromEmail;

    public NotificationService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public void sendEntryNotification(String farmerName, String productName,
                                      String classification, int lotNumber,
                                      double quantity, double totalPrice) {
        try {
            NotificationTemplate template = catalogService.getTemplate("ENTRY_REGISTERED", "EMAIL");
            String recipient = catalogService.getParameter("NOTIFICATION_DEFAULT_RECIPIENT");

            String body = render(template.getBodyTemplate(), Map.of(
                    "farmerName", farmerName,
                    "productName", productName,
                    "classification", classification,
                    "lotNumber", String.format("%02d", lotNumber),
                    "quantity", String.format("%.0f", quantity),
                    "totalPrice", String.format("%.2f", totalPrice)));

            send(recipient, template.getSubject(), body);
        } catch (Exception e) {
            // Una notificacion fallida no debe cancelar el registro de la entrada.
            log.error("No se pudo enviar la notificacion ENTRY_REGISTERED: {}", e.getMessage(), e);
        }
    }

    private String render(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private void send(String to, String subject, String body) throws java.io.IOException {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SENDGRID_API_KEY no configurada: se omite el envio a {} ({})", to, subject);
            return;
        }

        Mail mail = new Mail(new Email(fromEmail, "AGROBODEGA"), subject,
                new Email(to), new Content("text/plain", body));

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = new SendGrid(apiKey).api(request);
        log.info("Notificacion enviada a {} - estado {}", to, response.getStatusCode());
    }
}
