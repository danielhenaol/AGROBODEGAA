package com.agrobodega.application.service;

import com.agrobodega.domain.entity.NotificationTemplate;
import com.agrobodega.infrastructure.repository.AppParameterRepository;
import com.agrobodega.infrastructure.repository.MessageCatalogRepository;
import com.agrobodega.infrastructure.repository.NotificationTemplateRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Acceso a los catalogos guardados en la base de datos (Supabase):
 * mensajes, plantillas de notificacion y parametros.
 * Las consultas se guardan en cache (Caffeine) para no ir a la base cada vez.
 */
@Service
public class CatalogService {

    public static final String DEFAULT_LOCALE = "es-CO";

    private final MessageCatalogRepository messageRepository;
    private final NotificationTemplateRepository templateRepository;
    private final AppParameterRepository parameterRepository;

    public CatalogService(MessageCatalogRepository messageRepository,
                          NotificationTemplateRepository templateRepository,
                          AppParameterRepository parameterRepository) {
        this.messageRepository = messageRepository;
        this.templateRepository = templateRepository;
        this.parameterRepository = parameterRepository;
    }

    /** Devuelve el texto del mensaje; si no existe, devuelve el propio codigo. */
    @Cacheable(cacheNames = "messages", key = "#code", unless = "#result == #code")
    public String getMessage(String code) {
        return messageRepository.findByCodeAndLocaleAndActiveTrue(code, DEFAULT_LOCALE)
                .map(m -> m.getMessage())
                .orElse(code);
    }

    @Cacheable(cacheNames = "templates", key = "#eventCode + ':' + #channel")
    public NotificationTemplate getTemplate(String eventCode, String channel) {
        return templateRepository
                .findByEventCodeAndChannelAndLocaleAndActiveTrue(eventCode, channel, DEFAULT_LOCALE)
                .orElseThrow(() -> new IllegalStateException(
                        "Plantilla no configurada: " + eventCode + "/" + channel));
    }

    @Cacheable(cacheNames = "parameters", key = "#key")
    public String getParameter(String key) {
        return parameterRepository.findByParamKeyAndActiveTrue(key)
                .map(p -> p.getParamValue())
                .orElseThrow(() -> new IllegalStateException("Parametro no configurado: " + key));
    }

    public BigDecimal getDecimal(String key) {
        return new BigDecimal(getParameter(key));
    }
}
