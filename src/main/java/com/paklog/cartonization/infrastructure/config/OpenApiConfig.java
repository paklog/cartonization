package com.paklog.cartonization.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Cartonization Service}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("Local Development"),
                new Server().url("https://api.paklog.com").description("Production")
            ))
            .components(new Components()
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("Basic Authentication")
                )
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Bearer Token")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiInfo() {
        return new Info()
            .title(applicationName + " API")
            .version(applicationVersion)
            .description("""
                Cartonization Service API for optimizing package sizing and logistics.
                
                ## Features
                - **Packing Solution Calculation**: Optimize item placement in cartons
                - **Carton Management**: Manage available carton types and dimensions
                - **Product Catalog Integration**: Fetch product dimensions and attributes
                - **Event-Driven Architecture**: CloudEvents-compliant messaging
                - **Caching**: Redis-based caching for improved performance
                - **Circuit Breakers**: Resilient external service integration
                
                ## Getting Started
                1. Authenticate using Basic Auth or Bearer token
                2. Use `/api/v1/packing-solutions` to calculate optimal packing
                3. Manage cartons via `/api/v1/cartons` endpoints
                4. Monitor system health via `/actuator` endpoints
                
                ## Event Types
                The service publishes CloudEvents with type prefix: `com.paklog.cartonization`
                """)
            .termsOfService("https://paklog.com/terms")
            .contact(new Contact()
                .name("PakLog Development Team")
                .email("dev@paklog.com")
                .url("https://paklog.com/contact")
            )
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")
            );
    }
}