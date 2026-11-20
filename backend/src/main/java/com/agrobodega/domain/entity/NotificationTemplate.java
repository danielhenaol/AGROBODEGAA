package com.agrobodega.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_template")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_code", nullable = false)
    private String eventCode;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false)
    private String subject;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(nullable = false)
    private boolean active;

    public NotificationTemplate() {}

    public Long getId() { return id; }
    public String getEventCode() { return eventCode; }
    public String getChannel() { return channel; }
    public String getLocale() { return locale; }
    public String getSubject() { return subject; }
    public String getBodyTemplate() { return bodyTemplate; }
    public boolean isActive() { return active; }
}
