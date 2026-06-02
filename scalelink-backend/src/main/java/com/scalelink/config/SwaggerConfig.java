package com.scalelink.config;

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
 * Swagger / OpenAPI Configuration
 *
 * WHAT IS SWAGGER?
 * Swagger (now called OpenAPI) auto-generates interactive API documentation
 * from your controllers. It creates a web page where you can:
 * - See all your API endpoints
 * - See request/response schemas
 * - Test API calls directly from the browser
 *
 * ACCESS: http://localhost:8080/swagger-ui.html
 *
 * WHY IS THIS IMPORTANT?
 * 1. Frontend developers can see the API without reading backend code
 * 2. You can test APIs without Postman during development
 * 3. It serves as living documentation (always up-to-date with code)
 * 4. Interviewers love seeing it — shows professionalism
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScaleLink API")
                        .version("1.0.0")
                        .description("Production-grade URL Shortener Platform API. "
                                + "Demonstrates backend engineering, system design, "
                                + "and scalability concepts.")
                        .contact(new Contact()
                                .name("ScaleLink Engineering")
                                .email("contact@scalelink.io"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // Add JWT authentication to Swagger UI
                // This adds an "Authorize" button where you can paste your JWT token
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}
