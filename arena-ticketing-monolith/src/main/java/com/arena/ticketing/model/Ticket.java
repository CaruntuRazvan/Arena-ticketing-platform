package com.arena.ticketing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "tickets") // Am scos UniqueConstraint pentru a permite re-rezervarea locurilor expirate
@Getter @Setter @NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticketCode; // Îl vom genera în Service sau @PrePersist

    private java.time.LocalDateTime purchaseDate; // Data când devine CONFIRMED

    private java.time.LocalDateTime createdAt; // Data când a fost rezervat (pentru cele 15 min)

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.PENDING;

    private boolean used = false;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double finalPrice;

    // Se execută automat la prima salvare în DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
        if (this.ticketCode == null) {
            this.ticketCode = java.util.UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = TicketStatus.PENDING;
        }
    }
}