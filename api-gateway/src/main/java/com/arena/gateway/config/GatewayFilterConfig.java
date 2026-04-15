package com.arena.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class GatewayFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayFilterConfig.class);

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            // LOGICĂ DE REQUEST FILTERING
            logger.info("Gateway a interceptat cererea către: {}", exchange.getRequest().getPath());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                // LOGICĂ DE RESPONSE FILTERING
                exchange.getResponse().getHeaders().add("X-Arena-Gateway-Version", "1.0-PRO");
                logger.info("Gateway trimite răspunsul înapoi cu status: {}", exchange.getResponse().getStatusCode());
            }));
        };
    }
}