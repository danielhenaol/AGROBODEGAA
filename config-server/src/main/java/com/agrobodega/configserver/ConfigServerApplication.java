package com.agrobodega.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import java.util.Map;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ConfigServerApplication.class);

        app.setDefaultProperties(Map.of(
                "spring.application.name", "agrobodega-config-server",
                "server.port", "8888",
                "spring.profiles.active", "native",
                "spring.cloud.config.server.native.search-locations", "classpath:/config-repo",
                "management.endpoints.web.exposure.include", "health,info",
                "management.endpoint.health.show-details", "always"
        ));

        app.run(args);
    }
}