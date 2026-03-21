package com.arena.ticketing.service;

import com.arena.ticketing.dto.TicketResponseDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfGeneratorService pdfGeneratorService;

    public void sendTicketsEmail(String toEmail, List<TicketResponseDTO> tickets) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Biletele tale pentru meciul " + tickets.get(0).getOpponentName());
            helper.setText("Salut! \n\nÎți mulțumim pentru achiziție. Găsești atașate cele "
                    + tickets.size() + " bilete cumpărate.\n\nTe așteptăm pe stadion!");

            // Atașăm fiecare bilet ca PDF separat
            for (TicketResponseDTO ticket : tickets) {
                byte[] pdfBytes = pdfGeneratorService.generateTicketPdf(ticket);
                helper.addAttachment("Bilet_" + ticket.getTicketCode() + ".pdf",
                        new ByteArrayResource(pdfBytes));
            }

            mailSender.send(message);
            System.out.println("[EMAIL] Biletele au fost trimise cu succes către: " + toEmail);

        } catch (Exception e) {
            // Nu blocăm tranzacția dacă e-mailul eșuează, dar logăm eroarea
            System.err.println("[EMAIL ERROR] Nu s-au putut trimite biletele: " + e.getMessage());
        }
    }
}