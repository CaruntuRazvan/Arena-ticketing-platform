package com.arena.notification.controller;

import com.arena.notification.client.AuthClient;
import com.arena.notification.dto.external.MatchNotificationRequestDTO;
import com.arena.notification.dto.external.NotificationRequestDTO;
import com.arena.notification.dto.external.TicketResponseDTO;
import com.arena.notification.service.EmailService;
import com.arena.notification.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final AuthClient authClient;

    @PostMapping("/ticket")
    public void sendTicketNotification(@RequestBody List<TicketResponseDTO> tickets, @RequestParam String email) throws Exception {
        List<byte[]> pdfs = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();

        // Acum "tickets" este recunoscut pentru că este definit ca parametru mai sus
        for (TicketResponseDTO t : tickets) {
            byte[] pdf = pdfGeneratorService.generateTicketPdf(t);
            pdfs.add(pdf);

            // Generăm un nume de fișier curat
            String opponent = t.getOpponentName().replaceAll("[^a-zA-Z0-9]", "_");
            fileNames.add("bilet_" + opponent + "_" + t.getId() + ".pdf");
        }

        emailService.sendTicketWithAttachment(email, "Biletele tale pentru meci: ", pdfs, fileNames);
    }

    @PostMapping("/send-email")
    public ResponseEntity<Void> sendEmail(@RequestBody NotificationRequestDTO request) {
        emailService.sendSimpleEmail(request.getToEmail(), request.getSubject(), request.getBody());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/broadcast-match")
    public ResponseEntity<String> broadcastMatch(@RequestBody MatchNotificationRequestDTO request) {
        List<String> userEmails = authClient.getAllUserEmails();

        emailService.sendBulkMatchEmail(userEmails, request);

        return ResponseEntity.ok("Notificarile se trimit în fundal către " + userEmails.size() + " utilizatori.");
    }
}