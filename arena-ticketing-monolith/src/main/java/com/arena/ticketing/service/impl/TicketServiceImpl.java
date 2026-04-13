package com.arena.ticketing.service.impl;

import com.arena.ticketing.dto.MatchRevenueReportDTO;
import com.arena.ticketing.dto.TicketListDTO;
import com.arena.ticketing.dto.TicketRequestDTO;
import com.arena.ticketing.dto.TicketResponseDTO;
import com.arena.ticketing.exception.TicketException;
import com.arena.ticketing.model.*;
import com.arena.ticketing.repository.*;
import com.arena.ticketing.service.EmailService;
import com.arena.ticketing.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final MatchRepository matchRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final MatchSectorPriceRepository priceRepository;
    private final EmailService emailService;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<TicketResponseDTO> buyTickets(TicketRequestDTO request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new TicketException("Trebuie sa selectati cel putin un loc!");
        }
        int MAX_PER_TRANSACTION = 5;
        int MAX_TOTAL_PER_USER = 10;

        int requestedCount = request.getSeatIds().size();
        if (requestedCount > MAX_PER_TRANSACTION) {
            throw new TicketException("Puteti cumpara maxim 5 bilete per tranzactie!");
        }

        long alreadyOwned = ticketRepository.countByMatchIdAndUserId(request.getMatchId(), request.getUserId());
        if (alreadyOwned + requestedCount > MAX_TOTAL_PER_USER) {
            throw new TicketException("Limita depasita! Ai deja " + alreadyOwned +
                    " bilete pentru acest meci. Poti deține maxim " + MAX_TOTAL_PER_USER + " in total.");
        }
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new TicketException("Meciul nu a fost gasit!"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new TicketException("Utilizatorul nu a fost gasit!"));

        if (match.getStatus() == MatchStatus.CANCELLED) throw new TicketException("Meciul a fost anulat!");
        if (match.getStatus() == MatchStatus.FINISHED) throw new TicketException("Meciul s-a terminat!");
        if (match.getMatchDate().isBefore(LocalDateTime.now())) throw new TicketException("Meciul a trecut deja!");

        // Logica pentru discount
        boolean applyDiscount = request.isUseLoyaltyPoints() && user.getLoyaltyPoints() >= 10;
        double discountFactor = applyDiscount ? 0.9 : 1.0;

        List<Ticket> savedTickets = new ArrayList<>();
        List<TicketResponseDTO> responseList = new ArrayList<>();

        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);
        // 3. Procesăm fiecare loc din listă
        for (Long seatId : request.getSeatIds()) {

            // Verificăm dacă locul e ocupat
            if (ticketRepository.isSeatOccupied(match.getId(), seatId, timeout)) {
                throw new TicketException("Locul " + seatId + " este deja rezervat sau ocupat!");
            }

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new TicketException("Locul " + seatId + " nu a fost gasit!"));

            // Căutăm prețul pentru sectorul acestui scaun
            MatchSectorPrice priceConfig = priceRepository.findByMatchIdAndSectorId(
                    match.getId(),
                    seat.getSector().getId()
            ).orElseThrow(() -> new TicketException("Pretul pentru sectorul " + seat.getSector().getName() + " nu este configurat!"));

            // 4. Creăm biletul
            Ticket ticket = new Ticket();
            ticket.setMatch(match);
            ticket.setSeat(seat);
            ticket.setUser(user);
            //ticket.setFinalPrice(priceConfig.getPrice());
            ticket.setFinalPrice(priceConfig.getPrice() * discountFactor);
            ticket.setStatus(TicketStatus.PENDING);

            Ticket savedTicket = ticketRepository.save(ticket);

            responseList.add(mapToResponseDTO(savedTicket));
        }
        /*
        if (!responseList.isEmpty()) {
            emailService.sendTicketsEmail(user.getEmail(), responseList);
        }*/
        if (applyDiscount) {
            user.setLoyaltyPoints(user.getLoyaltyPoints() - 10);
            userRepository.save(user); // Salvăm noul sold de puncte (ex: de la 12 la 2)
        }
        return responseList;
    }

    @Override
    @Transactional
    public List<TicketResponseDTO> confirmPayment(List<Long> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        if (tickets.isEmpty()) throw new TicketException("Nu s-au găsit bilete pentru confirmare!");

        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);
        User user = tickets.get(0).getUser();

        for (Ticket ticket : tickets) {
            // Verificăm dacă rezervarea a expirat între timp
            if (ticket.getStatus() == TicketStatus.PENDING && ticket.getCreatedAt().isBefore(timeout)) {
                ticket.setStatus(TicketStatus.CANCELLED);
                ticketRepository.save(ticket);
                throw new TicketException("Rezervarea pentru biletul " + ticket.getId() + " a expirat și nu mai poate fi plătită!");
            }

            // Dacă e OK, îl confirmăm
            ticket.setStatus(TicketStatus.CONFIRMED);
            ticket.setPurchaseDate(LocalDateTime.now()); // Acum punem data plății
            ticketRepository.save(ticket);
        }

        int wonPoints = tickets.size();
        user.setLoyaltyPoints(user.getLoyaltyPoints() + wonPoints);
        userRepository.save(user);

        // ACUM trimitem emailul cu biletele oficiale
        List<TicketResponseDTO> confirmedDTOs = tickets.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        emailService.sendTicketsEmail(user.getEmail(), confirmedDTOs);

        return confirmedDTOs;
    }

    @Override
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketResponseDTO> getTicketsByMatch(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new TicketException("Meciul cu ID-ul " + matchId + " nu există.");
        }
        return ticketRepository.findByMatchId(matchId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketListDTO> getTicketsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new TicketException("Utilizatorul nu a fost găsit");
        }

        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        return tickets.stream().map(t -> new TicketListDTO(
                t.getId(),
                t.getTicketCode(),
                t.getMatch().getOpponentName(),
                t.getSeat().getSector().getName(), // Aici tragem sectorul
                t.getSeat().getRowNumber(),
                t.getSeat().getSeatNumber(),
                t.getFinalPrice(),
                t.isUsed()
        )).toList();
    }

    @Override
    public Double getTotalRevenueByMatch(Long matchId) {
        List<Ticket> matchTickets = ticketRepository.findByMatchId(matchId);
        return matchTickets.stream()
                .mapToDouble(Ticket::getFinalPrice)
                .sum();
    }

    @Override
    @Transactional
    public void validateTicket(String ticketCode) {
        // cautam biletul dupa codul UUID
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new TicketException("Cod invalid! Biletul nu există în sistem."));

        if (ticket.isUsed()) {
            throw new TicketException("Acces refuzat! Acest bilet a fost deja scanat.");
        }

        // Verificăm dacă meciul este în desfășurare sau urmează (nu în trecut)
        if (ticket.getMatch().getMatchDate().isBefore(java.time.LocalDateTime.now().minusHours(3))) {
            throw new TicketException("Acces refuzat! Acest bilet este pentru un eveniment care a trecut.");
        }

        ticket.setUsed(true);
        ticketRepository.save(ticket);
    }
    @Override
    public MatchRevenueReportDTO getDetailedRevenueReport(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TicketException("Meciul nu există"));

        List<Ticket> tickets = ticketRepository.findByMatchId(matchId);

        // Calculăm totalul general
        Double totalRevenue = tickets.stream()
                .mapToDouble(Ticket::getFinalPrice)
                .sum();

        // grupare pe sectoare și calculare suma + numărul de bilete per sector
        List<MatchRevenueReportDTO.SectorRevenueDTO> sectorDetails = tickets.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getSeat().getSector().getName(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            double sum = list.stream().mapToDouble(Ticket::getFinalPrice).sum();
                            return new MatchRevenueReportDTO.SectorRevenueDTO(
                                    list.get(0).getSeat().getSector().getName(),
                                    sum,
                                    (long) list.size()
                            );
                        })
                ))
                .values().stream().toList();

        return new MatchRevenueReportDTO(matchId, match.getOpponentName(), totalRevenue, sectorDetails);
    }

    @Override
    public TicketResponseDTO getTicketResponseById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketException("Biletul nu există!"));

        // Refolosim metoda de mapare pe care ai făcut-o deja!
        return mapToResponseDTO(ticket);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void performDatabaseHousekeeping() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        ticketRepository.deleteExpiredOrCancelledTickets(threshold);
        System.out.println("Job Curățenie: Biletele anulate sau expirate de peste 24h au fost șterse.");
    }

    private TicketResponseDTO mapToResponseDTO(Ticket t) {
        return new TicketResponseDTO(
                t.getId(),
                t.getTicketCode(),
                t.getMatch().getOpponentName(),
                t.getSeat().getSector().getName(),
                t.getSeat().getRowNumber(),
                t.getSeat().getSeatNumber(),
                t.getFinalPrice(),
                t.getStatus().name(),
                t.getCreatedAt()
        );
    }
}