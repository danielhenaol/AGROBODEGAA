package com.agrobodega.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "app_parameter")
public class AppParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_key", nullable = false, unique = true)
    private String paramKey;

    @Column(name = "param_value", nullable = false)
    private String paramValue;

    @Column(name = "value_type", nullable = false)
    private String valueType;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean active;

    public AppParameter() {}

    public Long getId() { return id; }
    public String getParamKey() { return paramKey; }
    public String getParamValue() { return paramValue; }
    public String getValueType() { return valueType; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
}
