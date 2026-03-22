package com.sunking.payg.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sun King PAYG Solar Backend API")
                        .description("""
                                REST API for the Sun King Pay-As-You-Go Solar System.

                                ## Features
                                - Customer Management
                                - Solar Device Registration & Assignment
                                - PAYG Payment Tracking
                                - Automatic Device Locking/Unlocking
                                - Mobile Money Gateway Integration
                                - Role-Based Access Control (Admin / Agent)

                                ## Authentication
                                Use `POST /api/v1/auth/login` to get a JWT token, then click **Authorize** above.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sun King Engineering")
                                .email("engineering@sunking.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://sunking.com")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from POST /api/v1/auth/login")));
    }
}
