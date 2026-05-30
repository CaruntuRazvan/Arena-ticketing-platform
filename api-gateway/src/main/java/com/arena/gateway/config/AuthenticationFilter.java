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
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Component
@Lazy
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private final ReactiveStringRedisTemplate redisTemplate;

    public AuthenticationFilter(ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    public static class Config { }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return stopWithStatus(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String token = (authHeader != null && authHeader.startsWith("Bearer "))
                    ? authHeader.substring(7) : authHeader;

            return redisTemplate.hasKey("blacklist:" + token)
                    .flatMap(isBlacklisted -> {
                        //System.out.println("Gateway verifică Redis. Găsit? " + isBlacklisted);
                        //System.out.println("DEBUG GATEWAY: Token-ul este in blacklist? " + isBlacklisted);
                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            return stopWithStatus(exchange, HttpStatus.UNAUTHORIZED);
                        }

                        try {
                            // Folosim parserBuilder pentru versiunea ta de JJWT
                            Claims claims = Jwts.parserBuilder()
                                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                                    .build()
                                    .parseClaimsJws(token)
                                    .getBody();

                            String path = exchange.getRequest().getPath().toString();
                            String method = exchange.getRequest().getMethod().name();

                            if (path.endsWith("/logout")) {
                                return chain.filter(exchange);
                            }
                            boolean isAdminPath = path.contains("/admin") || path.contains("/analytics");
                            boolean isUserTicketingAction = path.contains("/ticketing/buy") || path.contains("/ticketing/confirm");

                            if (isAdminPath || (!method.equals("GET") && !isUserTicketingAction)) {
                                String role = claims.get("role", String.class);
                                if (role == null || !role.equalsIgnoreCase("ADMIN")) {
                                    return stopWithStatus(exchange, HttpStatus.FORBIDDEN);
                                }
                            }
                            return chain.filter(exchange);
                        } catch (Exception e) {
                            System.out.println("EROARE JWT GATEWAY: " + e.getMessage());
                            return stopWithStatus(exchange, HttpStatus.UNAUTHORIZED);
                        }
                    });
        };
    }

    private Mono<Void> stopWithStatus(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}






