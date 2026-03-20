package com.arena.ticketing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Aceasta este "mașina de tocat" parole
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Dezactivăm pentru teste (Postman)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/h2-console/**").permitAll() // Liber la register
                        .anyRequest().authenticated() // Restul e blocat
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // Pentru consola H2

        return http.build();
    }
}