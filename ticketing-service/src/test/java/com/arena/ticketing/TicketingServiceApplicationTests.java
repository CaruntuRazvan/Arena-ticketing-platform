package com.arena.ticketing;

import com.arena.ticketing.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Noul import oficial pentru Spring Boot 3.4+
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                // Redirecționăm Feign local ca să ocolim Eureka
                "spring.cloud.openfeign.client.config.auth-service.url=http://localhost:8081",
                "spring.cloud.openfeign.client.config.catalog-service.url=http://localhost:8082",
                "spring.cloud.openfeign.client.config.notification-service.url=http://localhost:8084",

                // Excludem configurările automate de Postgres real și Redis
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
        }
)
@ActiveProfiles("test")
class TicketingServiceApplicationTests {

    // Înlocuitorul modern pentru @MockBean din versiunile noi de Spring Boot
    @MockitoBean
    private TicketRepository ticketRepository;

    @Test
    void contextLoads() {
        // Aplicația a pornit curat în mediu izolat de test!
    }
}