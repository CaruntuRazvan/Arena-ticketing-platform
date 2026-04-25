package com.arena.ticketing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticketCode;

    private LocalDateTime purchaseDate;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.PENDING;

    private boolean used = false;

    // Referințe către alte microservicii (doar ID-uri)
    private Long matchId;
    private Long seatId;
    private Long userId;

    private Double finalPrice;

    private Boolean mailSent = false;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.ticketCode == null) {
            this.ticketCode = UUID.randomUUID().toString();
        }
    }
}