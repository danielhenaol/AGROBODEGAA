package com.agrobodega.application.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void sendEntryNotification(String farmerName, String productName,
                                      String classification, int lotNumber,
                                      double quantity, double totalPrice) {
        Email from = new Email(fromEmail, "AGROBODEGA");
        Email to = new Email("danielhl1812@gmail.com");
        String subject = "Nueva entrada registrada - AGROBODEGA";
        String body = String.format(
                "Se ha registrado una nueva entrada:\n\n" +
                        "Cosechero: %s\n" +
                        "Producto: %s\n" +
                        "Clasificacion: %s\n" +
                        "Lote: %02d\n" +
                        "Cantidad: %.0f bultos\n" +
                        "Total: $%.2f\n\n" +
                        "Sistema AGROBODEGA",
                farmerName, productName, classification, lotNumber, quantity, totalPrice
        );
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("SendGrid status: " + response.getStatusCode());
            System.out.println("SendGrid body: " + response.getBody());
        } catch (IOException e) {
            System.out.println("Error enviando email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}