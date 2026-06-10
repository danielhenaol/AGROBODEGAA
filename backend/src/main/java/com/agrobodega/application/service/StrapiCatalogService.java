package com.agrobodega.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StrapiCatalogService {

    private final RestClient restClient;

    public StrapiCatalogService(@Value("${strapi.base-url}") String strapiBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(strapiBaseUrl)
                .build();
    }

    public String getMessageByCode(String code) {
        JsonNode response = restClient.get()
                .uri("/api/message-catalogs?filters[code][$eq]={code}", code)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("data") || response.get("data").isEmpty()) {
            return "Mensaje no encontrado en Strapi: " + code;
        }

        return response.get("data").get(0).get("message").asText();
    }

    public String getNotificationSubject(String eventCode) {
        JsonNode response = restClient.get()
                .uri("/api/notification-catalogs?filters[eventCode][$eq]={eventCode}", eventCode)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("data") || response.get("data").isEmpty()) {
            return "Notificación no encontrada en Strapi: " + eventCode;
        }

        return response.get("data").get(0).get("subject").asText();
    }

    public String getNotificationBodyTemplate(String eventCode) {
        JsonNode response = restClient.get()
                .uri("/api/notification-catalogs?filters[eventCode][$eq]={eventCode}", eventCode)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("data") || response.get("data").isEmpty()) {
            return "Plantilla no encontrada en Strapi: " + eventCode;
        }

        return response.get("data").get(0).get("bodyTemplate").asText();
    }
}