package com.arena.ticketing.service.impl;

import com.arena.ticketing.client.AuthClient;
import com.arena.ticketing.client.CatalogClient;
import com.arena.ticketing.client.NotificationClient;
import com.arena.ticketing.dto.*;
import com.arena.ticketing.dto.external.*;
import com.arena.ticketing.exception.TicketException;
import com.arena.ticketing.model.*;
import com.arena.ticketing.repository.TicketRepository;
import com.arena.ticketing.service.TicketService;
import com.arena.ticketing.service.impl.CatalogIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final NotificationClient notificationClient;
    private final CatalogIntegrationService catalogService;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<TicketResponseDTO> buyTickets(TicketRequestDTO request) {
        // 1. Validări de bază
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new TicketException("Trebuie sa selectati cel putin un loc!");
        }

        validateLimits(request); // helper function

        // 2. Apelăm Auth Service să vedem dacă userul există și ce puncte are
        UserDTO user = authClient.getUserById(request.getUserId());

        // 3. Apelăm Catalog Service pentru datele meciului
        //MatchDTO match = catalogClient.getMatchById(request.getMatchId());
        MatchDTO match = catalogService.getMatchSecurely(request.getMatchId());
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
            //SeatDTO seat = catalogClient.getSeatById(seatId);
            //Double basePrice = catalogClient.getPrice(match.getId(), seat.getSectorId());
            SeatDTO seat = catalogService.getSeatSecurely(seatId);
            Double basePrice = catalogService.getPriceSecurely(match.getId(), seat.getSectorId());

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
    @CircuitBreaker(name = "notificationCB", fallbackMethod = "fallbackForNotification")
    @Retry(name = "notificationRetry")
    public List<TicketResponseDTO> confirmPayment(List<Long> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);

        if (tickets.size() != ticketIds.size()) {
            throw new TicketException("Unul sau mai multe bilete nu au fost găsite!");
        }

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.PENDING &&
                    ticket.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {

                ticket.setStatus(TicketStatus.CANCELLED);
                ticketRepository.save(ticket);
                throw new TicketException("Rezervarea pentru biletul " + ticket.getId() + " a expirat!");
            }
        }

        Long userId = tickets.get(0).getUserId();
        UserDTO user = authClient.getUserById(userId);
        List<TicketResponseDTO> confirmedDTOs = new ArrayList<>();

        for (Ticket ticket : tickets) {

            ticket.setStatus(TicketStatus.CONFIRMED);
            ticket.setPurchaseDate(LocalDateTime.now());
            ticket.setMailSent(true);
            ticketRepository.save(ticket);

            //MatchDTO match = catalogClient.getMatchById(ticket.getMatchId());
            MatchDTO match = catalogService.getMatchSecurely(ticket.getMatchId());
            TicketResponseDTO dto = mapToResponseDTO(ticket, match, null);
            confirmedDTOs.add(dto);
        }
        notificationClient.sendTicketNotification(confirmedDTOs, user.getEmail());
        authClient.updatePoints(userId, tickets.size());
        return confirmedDTOs;
    }
    /*
    @Override
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(t -> mapToResponseDTO(t, catalogService.getMatchSecurely(t.getMatchId()), null))
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketResponseDTO> getTicketsByMatch(Long matchId) {
        //MatchDTO match = catalogClient.getMatchById(matchId);
        MatchDTO match = catalogService.getMatchSecurely(matchId);
        return ticketRepository.findByMatchId(matchId).stream()
                .map(t -> mapToResponseDTO(t, match, null))
                .collect(Collectors.toList());
    }


    @Override
    public List<TicketListDTO> getTicketsByUserId(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(t -> {
                    //MatchDTO match = catalogClient.getMatchById(t.getMatchId());
                    MatchDTO match = catalogService.getMatchSecurely(t.getMatchId());
                    //SeatDTO seat = catalogClient.getSeatById(t.getSeatId());
                    SeatDTO seat = catalogService.getSeatSecurely(t.getSeatId());
                    return new TicketListDTO(
                            t.getId(), t.getTicketCode(), match.getOpponentName(),
                            "Sector " + seat.getSectorId(), seat.getRowNumber(), seat.getSeatNumber(),
                            t.getFinalPrice(), t.isUsed()
                    );
                }).collect(Collectors.toList());
    }
    */
    @Override
    public Page<TicketListDTO> getTicketsByUserId(Long userId, Pageable pageable) {
        return ticketRepository.findByUserId(userId, pageable)
                .map(t -> {
                    MatchDTO match = catalogService.getMatchSecurely(t.getMatchId());
                    SeatDTO seat = catalogService.getSeatSecurely(t.getSeatId());

                    // Calculăm dinamic dacă meciul a trecut deja de 3 ore
                    boolean matchHasPassed = match.getMatchDate().plusHours(3).isBefore(LocalDateTime.now());

                    // Biletul este considerat indisponibil/inactiv dacă a fost deja folosit SAU dacă meciul a trecut
                    boolean isUsedOrExpired = t.isUsed() || matchHasPassed;

                    return new TicketListDTO(
                            t.getId(), t.getTicketCode(), match.getOpponentName(),
                            "Sector " + seat.getSectorId(), seat.getRowNumber(), seat.getSeatNumber(),
                            t.getFinalPrice(), isUsedOrExpired // 👈 Trimitem true dacă e folosit sau expirat
                    );
                });
    }

    @Override
    public Page<TicketResponseDTO> getTicketsByMatch(Long matchId, Pageable pageable) {
        MatchDTO match = catalogService.getMatchSecurely(matchId);
        return ticketRepository.findByMatchId(matchId, pageable)
                .map(t -> mapToResponseDTO(t, match, null));
    }

    @Override
    public Page<TicketResponseDTO> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(t -> mapToResponseDTO(t, catalogService.getMatchSecurely(t.getMatchId()), null));
    }
    @Override
    @Transactional
    public void validateTicket(String ticketCode) {

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new TicketException("Cod invalid! Biletul nu există în sistem."));

        if (ticket.isUsed()) {
            throw new TicketException("Acces refuzat! Acest bilet a fost deja scanat.");
        }

        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new TicketException("Acces refuzat! Plata biletului nu a fost confirmată.");
        }

        //MatchDTO match = catalogClient.getMatchById(ticket.getMatchId());
        MatchDTO match = catalogService.getMatchSecurely(ticket.getMatchId());

        if (match.getMatchDate().isBefore(LocalDateTime.now().minusHours(3))) {
            throw new TicketException("Acces refuzat! Acest bilet este pentru un eveniment care a trecut.");
        }

        ticket.setUsed(true);
        ticketRepository.save(ticket);
    }

    @Scheduled(cron = "0 0 * * * *") // Rulam o data pe ora pentru curatenia meciurilor trecute
    @Transactional
    public void performDatabaseHousekeeping() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(15);

        try {
            // 1. Logica ta existenta pentru stergerea rezervarilor temporare expirate (PENDING)
            ticketRepository.deleteExpiredOrCancelledTickets(threshold);

            // 2. LOGICA NOUĂ: Expirarea biletelor nefolosite de la meciuri trecute
            List<Long> activeMatchIds = ticketRepository.findDistinctMatchIdsWithConfirmedTickets();

            for (Long matchId : activeMatchIds) {
                try {
                    MatchDTO match = catalogService.getMatchSecurely(matchId);
                    // Daca data meciului + 3 ore este in trecut
                    if (match.getMatchDate().plusHours(3).isBefore(now)) {
                        ticketRepository.expireUnusedTicketsForMatch(matchId);
                        System.out.println(">>> HOUSEKEEPING: Biletele nefolosite pentru meciul #" + matchId + " au fost marcate ca expirate.");
                    }
                } catch (Exception e) {
                    System.out.println(">>> HOUSEKEEPING WARN: Nu s-au putut procesa biletele pentru meciul #" + matchId);
                }
            }
        } catch (Exception e) {
            System.out.println(">>> HOUSEKEEPING ERROR: " + e.getMessage());
        }
    }

    // 1. FALLBACK
    public List<TicketResponseDTO> fallbackForNotification(List<Long> ticketIds, Throwable t) {
        if (t instanceof TicketException) {
            throw (TicketException) t;
        }

        System.err.println(">>> FALLBACK ACTIVAT: Notification Service este indisponibil!");

        // În caz de fallback, returnăm biletele din DB marcate cu mailSent = false
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        List<TicketResponseDTO> response = new ArrayList<>();

        for (Ticket ticket : tickets) {
            ticket.setStatus(TicketStatus.CONFIRMED);
            ticket.setPurchaseDate(LocalDateTime.now());
            ticket.setMailSent(false); //false in caz de eroare
            ticketRepository.save(ticket);

            //MatchDTO match = catalogClient.getMatchById(ticket.getMatchId());
            MatchDTO match = catalogService.getMatchSecurely(ticket.getMatchId());
            response.add(mapToResponseDTO(ticket, match, null));
        }

        System.out.println("Biletele au fost confirmate, dar mail-ul a eșuat. mailSent = false în DB.");
        return response;
    }

    @Override
    public List<Long> getOccupiedSeatsInList(Long matchId, List<Long> seatIds) {
        // Considerăm locurile PENDING ca fiind ocupate doar dacă au fost create în ultimele 10 minute
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(10);

        return ticketRepository.findOccupiedSeatIdsInList(matchId, seatIds, timeout);
    }
    // helper
    private void validateLimits(TicketRequestDTO request) {
        int requestedCount = request.getSeatIds().size();
        if (requestedCount > 5) throw new TicketException("Maxim 5 bilete per tranzactie!");

        long alreadyOwned = ticketRepository.countByMatchIdAndUserId(request.getMatchId(), request.getUserId());
        if (alreadyOwned + requestedCount > 10) throw new TicketException("Limita de 10 bilete depasita!");
    }

    @Override
    public MatchRevenueReportDTO getDetailedRevenueReport(Long matchId) {
        com.arena.ticketing.dto.external.MatchDTO match = catalogService.getMatchSecurely(matchId);

        // Extragem doar biletele CONFIRMED
        java.util.List<com.arena.ticketing.model.Ticket> confirmedTickets = ticketRepository.findByMatchId(matchId).stream()
                .filter(t -> com.arena.ticketing.model.TicketStatus.CONFIRMED.equals(t.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        // Calculam sumele si volumele globale la nivel de meci
        double totalRevenue = confirmedTickets.stream().mapToDouble(com.arena.ticketing.model.Ticket::getFinalPrice).sum();
        long totalTicketsSold = confirmedTickets.size();

        java.util.Map<java.lang.Long, java.util.List<com.arena.ticketing.model.Ticket>> ticketsBySectorId = confirmedTickets.stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> {
                    com.arena.ticketing.dto.external.SeatDTO seat = catalogService.getSeatSecurely(t.getSeatId());
                    return seat.getSectorId(); // Grupăm nativ după ID-ul numeric al sectorului
                }));

        java.util.List<MatchRevenueReportDTO.SectorRevenueDTO> sectorsAnalytics = new java.util.ArrayList<>();

        for (java.util.Map.Entry<java.lang.Long, java.util.List<com.arena.ticketing.model.Ticket>> entry : ticketsBySectorId.entrySet()) {
            java.lang.Long sectorId = entry.getKey();
            java.util.List<com.arena.ticketing.model.Ticket> sectorTickets = entry.getValue();

            long ticketsSold = sectorTickets.size();
            double sectorRevenue = sectorTickets.stream().mapToDouble(com.arena.ticketing.model.Ticket::getFinalPrice).sum();

            // Construim DTO-ul secundar apelând constructorul în ordinea exactă: (Long sectorId, Long ticketsSold, Double revenue)
            MatchRevenueReportDTO.SectorRevenueDTO sectorDTO = new MatchRevenueReportDTO.SectorRevenueDTO(
                    sectorId,
                    ticketsSold,
                    sectorRevenue
            );
            sectorsAnalytics.add(sectorDTO);
        }

        // 6. Returnăm raportul agreat gata formatat pentru a fi trimis ca JSON
        return new MatchRevenueReportDTO(
                matchId,
                match.getOpponentName(),
                totalRevenue,
                totalTicketsSold,
                sectorsAnalytics
        );
    }

    private TicketResponseDTO mapToResponseDTO(Ticket t, MatchDTO match, SeatDTO seat) {
        if (seat == null) seat = catalogService.getSeatSecurely(t.getSeatId());
        String computedStatus = t.getStatus().name();

        if ("CONFIRMED".equals(computedStatus) && !t.isUsed() && match.getMatchDate().plusHours(3).isBefore(LocalDateTime.now())) {
            computedStatus = "CANCELLED"; // Marcam ca expirat daca meciul a trecut de 3 ore si biletul nu a fost folosit
        }
        return new TicketResponseDTO(
                t.getId(),
                t.getTicketCode(),
                match.getOpponentName(),
                match.getMatchDate(),
                "Sector " + seat.getSectorId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                t.getFinalPrice(),
                computedStatus,
                t.getCreatedAt()
        );
    }
}