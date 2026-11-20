package com.agrobodega.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** Activa la cache en memoria (Caffeine). Ajustes en application.properties. */
@Configuration
@EnableCaching
public class CacheConfig {
}
