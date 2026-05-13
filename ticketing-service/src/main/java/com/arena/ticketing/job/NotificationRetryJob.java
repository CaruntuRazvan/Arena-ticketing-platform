package com.arena.ticketing.job;

import com.arena.ticketing.client.AuthClient;
import com.arena.ticketing.client.CatalogClient;
import com.arena.ticketing.client.NotificationClient;
import com.arena.ticketing.dto.external.MatchDTO;
import com.arena.ticketing.dto.TicketResponseDTO;
import com.arena.ticketing.dto.external.SeatDTO;
import com.arena.ticketing.dto.external.UserDTO;
import com.arena.ticketing.model.Ticket;
import com.arena.ticketing.model.TicketStatus;
import com.arena.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryJob {

    private final TicketRepository ticketRepository;
    private final NotificationClient notificationClient;
    private final AuthClient authClient;
    private final CatalogClient catalogClient;

    @Scheduled(fixedDelay = 60000)
    public void retrySendingMails() {
        List<Ticket> pendingTickets = ticketRepository.findByStatusAndMailSentFalse(TicketStatus.CONFIRMED);

        if (pendingTickets.isEmpty()) {
            return;
        }

        Map<Long, List<Ticket>> ticketsByUser = pendingTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getUserId));

        log.info("S-au găsit {} bilete netrimise. Încercăm gruparea pe {} utilizatori...",
                pendingTickets.size(), ticketsByUser.size());

        for (Map.Entry<Long, List<Ticket>> entry : ticketsByUser.entrySet()) {
            Long userId = entry.getKey();
            List<Ticket> userTickets = entry.getValue();

            try {
                UserDTO user = authClient.getUserById(userId);
                List<TicketResponseDTO> dtos = new ArrayList<>();

                // Colectăm toate DTO-urile pentru acest utilizator
                for (Ticket ticket : userTickets) {
                    MatchDTO match = catalogClient.getMatchById(ticket.getMatchId());
                    dtos.add(mapToResponseDTO(ticket, match));
                }

                // Trimitem lista (Notification Service trebuie să accepte List acum)
                notificationClient.sendTicketNotification(dtos, user.getEmail());

                // Marcăm și salvăm tot grupul ca fiind trimis
                for (Ticket ticket : userTickets) {
                    ticket.setMailSent(true);
                }
                ticketRepository.saveAll(userTickets);

                log.info("Mail trimis cu succes pentru user: {}, bilete: {}",
                        userId, userTickets.size());

            } catch (Exception e) {
                log.error("Notification Service indisponibil pentru userul {}. Reîncercăm...", userId);
            }
        }
    }

    private TicketResponseDTO mapToResponseDTO(Ticket t, MatchDTO match) {

        SeatDTO seat = catalogClient.getSeatById(t.getSeatId());

        return new TicketResponseDTO(
                t.getId(),
                t.getTicketCode(),
                match.getOpponentName(),
                match.getMatchDate(),
                "Sector " + seat.getSectorId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                t.getFinalPrice(),
                t.getStatus().name(),
                t.getCreatedAt()
        );
    }
}