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

    private final StrapiCatalogService strapiCatalogService;

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public NotificationService(StrapiCatalogService strapiCatalogService) {
        this.strapiCatalogService = strapiCatalogService;
    }

    public void sendEntryNotification(String farmerName, String productName,
                                      String classification, int lotNumber,
                                      double quantity, double totalPrice) {

        String subject = strapiCatalogService.getNotificationSubject("ENTRY_REGISTERED");

        String body = strapiCatalogService.getNotificationBodyTemplate("ENTRY_REGISTERED")
                .replace("{{farmerName}}", farmerName)
                .replace("{{productName}}", productName)
                .replace("{{classification}}", classification)
                .replace("{{lotNumber}}", String.format("%02d", lotNumber))
                .replace("{{quantity}}", String.format("%.0f", quantity))
                .replace("{{totalPrice}}", String.format("%.2f", totalPrice));

        Email from = new Email(fromEmail, "AGROBODEGA");
        Email to = new Email("danielhl1812@gmail.com");

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