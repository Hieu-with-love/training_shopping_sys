package com.training.shopping_sys.config;

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

/**
 * Swagger/OpenAPI Configuration.
 * 
 * <p>Configures Swagger UI and OpenAPI documentation for the REST APIs.
 * Provides interactive API documentation with authentication support.</p>
 * 
 * <p>Features:
 * - API metadata (title, version, description, contact)
 * - Server configuration (development server)
 * - Security schemes (Basic Auth and Cookie-based session)
 * </p>
 * 
 * <p>Access Swagger UI at: http://localhost:8080/swagger-ui.html</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configure custom OpenAPI specification.
     * 
     * <p>Creates OpenAPI 3.0 specification with:
     * - API information and metadata
     * - Development server configuration
     * - Security schemes for authentication
     * - Global security requirements
     * </p>
     * 
     * @return Configured OpenAPI object
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shopping System API")
                        .version("1.0.0")
                        .description("API documentation for Shopping System - Hệ thống quản lý mua sắm")
                        .contact(new Contact()
                                .name("Shopping System Team")
                                .email("support@shopping-sys.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server")))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Basic Authentication với username/password"))
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Session cookie sau khi login")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("basicAuth")
                        .addList("cookieAuth"));
    }
}
