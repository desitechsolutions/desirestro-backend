package com.dts.restro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * Access Swagger UI at: <a href="http://localhost:8080/swagger-ui.html">swagger-ui.html</a>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI desiRestroOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("DesiRestro – Restaurant Management API")
                        .description("Production-grade REST API for managing restaurant tables, " +
                                "menus, Kitchen Order Tickets (KOT), billing, staff, inventory and analytics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DesiTech Solutions")
                                .url("https://github.com/desitechsolutions"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://github.com/desitechsolutions/desirestro-backend")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide a valid JWT access token obtained from POST /api/auth/login")));
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
