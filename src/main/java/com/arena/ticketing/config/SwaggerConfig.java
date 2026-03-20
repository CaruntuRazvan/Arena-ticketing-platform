package com.arena.ticketing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Arena Ticketing API")
                        .version("1.0")
                        .description("Sistem de gestionare a biletelor pentru meciuri de fotbal.")
                        .contact(new Contact()
                                .name("Caruntu Razvan")
                                .email("razvan.caruntu@s.unibuc.ro")));
    }
}