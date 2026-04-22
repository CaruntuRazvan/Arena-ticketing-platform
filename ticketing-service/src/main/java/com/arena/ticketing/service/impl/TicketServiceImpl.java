package com.arena.ticketing.service.impl;

import com.arena.ticketing.client.AuthClient;
import com.arena.ticketing.client.CatalogClient;
import com.arena.ticketing.dto.*;
import com.arena.ticketing.dto.external.*;
import com.arena.ticketing.exception.TicketException;
import com.arena.ticketing.model.*;
import com.arena.ticketing.repository.TicketRepository;
import com.arena.ticketing.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    // Înlocuim repository-urile externe cu clienți Feign
    private final CatalogClient catalogClient;
    private final AuthClient authClient;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<TicketResponseDTO> buyTickets(TicketRequestDTO request) {
        // 1. Validări de bază
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new TicketException("Trebuie sa selectati cel putin un loc!");
        }

        // 2. Apelăm Auth Service să vedem dacă userul există și ce puncte are
        UserDTO user = authClient.getUserById(request.getUserId());

        // 3. Apelăm Catalog Service pentru datele meciului
        MatchDTO match = catalogClient.getMatchById(request.getMatchId());

        // Verificări status meci (folosind datele primite prin Feign)
        if (MatchStatus.CANCELLED.equals(match.getStatus())) {
            throw new TicketException("Meciul a fost anulat!");
        }
        if (match.getMatchDate().isBefore(LocalDateTime.now())) throw new TicketException("Meciul s-a terminat deja!");

        // Logica de discount (Loyalty)
        boolean applyDiscount = request.isUseLoyaltyPoints() && user.getLoyaltyPoints() != null && user.getLoyaltyPoints() >= 10;
        double discountFactor = applyDiscount ? 0.9 : 1.0;

        List<Ticket> savedTickets = new ArrayList<>();
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);

        for (Long seatId : request.getSeatIds()) {
            // Verificăm în baza noastră locală dacă locul e deja ocupat la acest meci
            if (ticketRepository.isSeatOccupied(match.getId(), seatId, timeout)) {
                throw new TicketException("Locul " + seatId + " este deja rezervat!");
            }

            // Luăm detaliile scaunului și prețul din Catalog
            SeatDTO seat = catalogClient.getSeatById(seatId);
            Double basePrice = catalogClient.getPrice(match.getId(), seat.getSectorId());

            Ticket ticket = new Ticket();
            ticket.setMatchId(match.getId());
            ticket.setSeatId(seatId);
            ticket.setUserId(user.getId());
            ticket.setFinalPrice(basePrice * discountFactor);
            ticket.setStatus(TicketStatus.PENDING);

            savedTickets.add(ticketRepository.save(ticket));
        }

        // Dacă s-a folosit discount, anunțăm Auth Service să scadă punctele
        if (applyDiscount) {
            authClient.updatePoints(user.getId(), -10);
        }

        return savedTickets.stream().map(t -> mapToResponseDTO(t, match, null)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TicketResponseDTO> confirmPayment(List<Long> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        if (tickets.isEmpty()) throw new TicketException("Nu s-au găsit bilete!");

        Long userId = tickets.get(0).getUserId();

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.PENDING &&
                    ticket.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
                ticket.setStatus(TicketStatus.CANCELLED);
                ticketRepository.save(ticket);
                throw new TicketException("Rezervarea a expirat!");
            }

            ticket.setStatus(TicketStatus.CONFIRMED);
            ticket.setPurchaseDate(LocalDateTime.now());
            ticketRepository.save(ticket);
        }

        // Adăugăm puncte de loialitate prin Auth Service
        authClient.updatePoints(userId, tickets.size());

        return tickets.stream()
                .map(t -> mapToResponseDTO(t, catalogClient.getMatchById(t.getMatchId()), null))
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(t -> mapToResponseDTO(t, catalogClient.getMatchById(t.getMatchId()), null))
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketResponseDTO> getTicketsByMatch(Long matchId) {
        MatchDTO match = catalogClient.getMatchById(matchId);
        return ticketRepository.findByMatchId(matchId).stream()
                .map(t -> mapToResponseDTO(t, match, null))
                .collect(Collectors.toList());
    }


    @Override
    public List<TicketListDTO> getTicketsByUserId(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(t -> {
                    MatchDTO match = catalogClient.getMatchById(t.getMatchId());
                    SeatDTO seat = catalogClient.getSeatById(t.getSeatId());
                    return new TicketListDTO(
                            t.getId(), t.getTicketCode(), match.getOpponentName(),
                            "Sector " + seat.getSectorId(), seat.getRowNumber(), seat.getSeatNumber(),
                            t.getFinalPrice(), t.isUsed()
                    );
                }).collect(Collectors.toList());
    }
    /*
    @Override
    @Transactional
    public void validateTicket(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new TicketException("Cod invalid!"));

        if (ticket.isUsed()) throw new TicketException("Bilet deja scanat!");

        MatchDTO match = catalogClient.getMatchById(ticket.getMatchId());
        if (match.getMatchDate().isBefore(LocalDateTime.now().minusHours(3))) {
            throw new TicketException("Meciul a expirat!");
        }

        ticket.setUsed(true);
        ticketRepository.save(ticket);
    }
    */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void performDatabaseHousekeeping() {
        ticketRepository.deleteExpiredOrCancelledTickets(LocalDateTime.now().minusHours(24));
    }

    // helper

    private void validateLimits(TicketRequestDTO request) {
        int requestedCount = request.getSeatIds().size();
        if (requestedCount > 5) throw new TicketException("Maxim 5 bilete per tranzactie!");

        long alreadyOwned = ticketRepository.countByMatchIdAndUserId(request.getMatchId(), request.getUserId());
        if (alreadyOwned + requestedCount > 10) throw new TicketException("Limita de 10 bilete depasita!");
    }

    private TicketResponseDTO mapToResponseDTO(Ticket t, MatchDTO match, SeatDTO seat) {
        if (seat == null) seat = catalogClient.getSeatById(t.getSeatId());
        return new TicketResponseDTO(
                t.getId(), t.getTicketCode(), match.getOpponentName(),
                "Sector " + seat.getSectorId(), seat.getRowNumber(), seat.getSeatNumber(),
                t.getFinalPrice(), t.getStatus().name(), t.getCreatedAt()
        );
    }
}