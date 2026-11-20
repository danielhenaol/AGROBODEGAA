package com.agrobodega.notification.infrastructure;

import com.agrobodega.notification.domain.NotificationTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Lee las plantillas de la tabla notification_template (Supabase). */
@Repository
public class TemplateRepository {

    private static final String DEFAULT_LOCALE = "es-CO";

    private final JdbcClient jdbc;

    public TemplateRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<NotificationTemplate> find(String eventCode, String channel) {
        return jdbc.sql("""
                        SELECT event_code, channel, subject, body_template
                        FROM notification_template
                        WHERE event_code = :eventCode
                          AND channel = :channel
                          AND locale = :locale
                          AND active = TRUE
                        """)
                .param("eventCode", eventCode)
                .param("channel", channel)
                .param("locale", DEFAULT_LOCALE)
                .query((rs, i) -> new NotificationTemplate(
                        rs.getString("event_code"),
                        rs.getString("channel"),
                        rs.getString("subject"),
                        rs.getString("body_template")))
                .optional();
    }
}
