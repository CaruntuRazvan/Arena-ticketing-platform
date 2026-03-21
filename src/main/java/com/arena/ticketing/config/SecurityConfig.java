package com.arena.ticketing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Rute complet PUBICE (oricine le vede)
                        .requestMatchers("/api/users/register", "/api/users/login", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/matches/**", "/api/stadiums/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/matches/**", "/api/stadiums/**", "/api/prices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/matches/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAuthority("ADMIN")

                        .requestMatchers("/api/tickets/buy").hasAnyAuthority("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()) // Activează Basic Auth pentru Postman
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}