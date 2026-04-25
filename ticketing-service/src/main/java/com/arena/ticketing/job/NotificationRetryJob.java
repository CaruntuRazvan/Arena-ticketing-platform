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

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryJob {

    private final TicketRepository ticketRepository;
    private final NotificationClient notificationClient;
    private final AuthClient authClient;
    private final CatalogClient catalogClient;

    // Rulează la fiecare 60 secunde
    @Scheduled(fixedDelay = 60000)
    public void retrySendingMails() {
        // Căutăm biletele confirmate care au mail_sent = false sau null
        List<Ticket> pendingTickets = ticketRepository.findByStatusAndMailSentFalse(TicketStatus.CONFIRMED);

        if (pendingTickets.isEmpty()) {
            return;
        }

        log.info("S-au găsit {} bilete cu mail netrimis. Încercăm retrimiterea...", pendingTickets.size());

        for (Ticket ticket : pendingTickets) {
            try {
                UserDTO user = authClient.getUserById(ticket.getUserId());
                MatchDTO match = catalogClient.getMatchById(ticket.getMatchId());

                // Mapăm biletul la DTO (folosește metoda ta de mapare)
                TicketResponseDTO dto = mapToResponseDTO(ticket, match);

                notificationClient.sendTicketNotification(dto, user.getEmail());

                ticket.setMailSent(true);
                ticketRepository.save(ticket);
                log.info("Mail trimis cu succes în fundal pentru biletul: {}", ticket.getId());

            } catch (Exception e) {
                log.error("Notification Service este încă indisponibil pentru biletul {}. Reîncercăm data viitoare.", ticket.getId());
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