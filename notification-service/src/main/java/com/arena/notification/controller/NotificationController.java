package com.arena.notification.controller;

import com.arena.notification.dto.external.NotificationRequestDTO;
import com.arena.notification.dto.external.TicketResponseDTO;
import com.arena.notification.service.EmailService;
import com.arena.notification.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;

    @PostMapping("/ticket")
    public void sendTicketNotification(@RequestBody TicketResponseDTO ticket, @RequestParam String email) throws Exception {
        byte[] pdf = pdfGeneratorService.generateTicketPdf(ticket);

        emailService.sendTicketWithAttachment(email, "Biletul tău: " + ticket.getOpponentName(), pdf, "bilet.pdf");
    }

    @PostMapping("/send-email")
    public ResponseEntity<Void> sendEmail(@RequestBody NotificationRequestDTO request) {
        emailService.sendSimpleEmail(request.getToEmail(), request.getSubject(), request.getBody());
        return ResponseEntity.ok().build();
    }
}