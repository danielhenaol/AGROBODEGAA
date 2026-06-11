package com.agrobodega.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server railwayServer = new Server();
        railwayServer.setUrl("https://agrobodegaa-production-c214.up.railway.app");
        railwayServer.setDescription("Railway Production Server");

        return new OpenAPI()
                .servers(List.of(railwayServer));
    }
}