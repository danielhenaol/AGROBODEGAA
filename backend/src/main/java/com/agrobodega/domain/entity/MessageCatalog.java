package com.agrobodega.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "message_catalog")
public class MessageCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false)
    private String type;

    @Column
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private boolean active;

    public MessageCatalog() {}

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getLocale() { return locale; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isActive() { return active; }
}
