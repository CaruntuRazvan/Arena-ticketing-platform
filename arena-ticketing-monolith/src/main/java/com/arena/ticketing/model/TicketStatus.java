package com.arena.ticketing.model;

public enum TicketStatus {
    PENDING,    // Rezervat (blochează locul 15 minute)
    CONFIRMED,  // Plătit (locul este ocupat definitiv)
    CANCELLED   // Anulat/Expirat
}