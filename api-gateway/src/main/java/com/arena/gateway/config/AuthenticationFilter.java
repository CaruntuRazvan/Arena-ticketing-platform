package com.arena.gateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;

@Component
@Lazy
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${app.jwt.secret}")
    private String secretKey;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config { }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 1. Verificăm dacă există header-ul Authorization (Token-ul)
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return stopWithStatus(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

            // 2. Curățăm token-ul (scoatem "Bearer " din față)
            String token = (authHeader != null && authHeader.startsWith("Bearer "))
                    ? authHeader.substring(7) : authHeader;

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String path = exchange.getRequest().getPath().toString();
                String method = exchange.getRequest().getMethod().name();

                if (path.contains("/admin") || !method.equals("GET")) {
                    String role = claims.get("role", String.class); // Presupunem că în Auth Service pui rolul în claim-ul "role"

                    if (role == null || !role.equalsIgnoreCase("ADMIN")) {
                        return stopWithStatus(exchange, HttpStatus.FORBIDDEN); // 403 dacă nu e admin
                    }
                }

                return chain.filter(exchange);
            } catch (Exception e) {

                return stopWithStatus(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> stopWithStatus(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}