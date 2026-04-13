package com.arena.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArenaTicketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArenaTicketingApplication.class, args);
    }

}
