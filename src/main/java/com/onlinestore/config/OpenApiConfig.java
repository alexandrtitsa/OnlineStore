package com.onlinestore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Online Store API")
                        .version("1.0.0")
                        .description("""
                    ## Hexagonal Architecture E-commerce API
                    
                    This API demonstrates a clean hexagonal architecture implementation with:
                    - **Domain-Driven Design** - Pure business logic in domain layer
                    - **CQRS Pattern** - Separate commands and queries
                    - **Event-Driven Architecture** - Domain events for side effects
                    - **JWT Authentication** - Secure token-based auth
                    - **Role-Based Access Control** - USER and ADMIN roles
                    
                    ### Authentication Flow
                    1. Register a new account at `/api/auth/register`
                    2. Login to get JWT token at `/api/auth/login`
                    3. Click **Authorize** button and enter: `Bearer <your-token>`
                    4. Now you can access protected endpoints
                    
                    ### Test Accounts
                    - **User**: `user@example.com` / `password123`
                    - **Admin**: `admin@example.com` / `password123`
                    """)
                        .contact(new Contact()
                                .name("Online Store Team")
                                .email("support@onlinestore.com")
                                .url("https://github.com/yourusername/online-store"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.onlinestore.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login")));
    }
}