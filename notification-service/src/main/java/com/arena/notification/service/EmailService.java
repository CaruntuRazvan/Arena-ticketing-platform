package com.arena.notification.service;
import com.arena.notification.exception.NotificationException;
import com.arena.notification.dto.external.MatchNotificationRequestDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@RefreshScope
@Slf4j
public class EmailService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JavaMailSender mailSender;
    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendTicketWithAttachment(String toEmail, String subject, List<byte[]> allPdfBytes, List<String> fileNames) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText("<p>Salutare!</p><p>Atașat găsești biletul/biletele tale pentru meci. Ne vedem pe stadion!</p>", true);
            // Adăugăm fiecare bilet ca atasament separat in același mail
            for (int i = 0; i < allPdfBytes.size(); i++) {
                helper.addAttachment(fileNames.get(i), new ByteArrayResource(allPdfBytes.get(i)));
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Eroare la compunerea email-ului cu atașament către {}", toEmail);
            throw e;
        }
    }

    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Eroare la trimiterea mail-ului către {}: {}", to, e.getMessage());
            throw new NotificationException("Eșec la trimiterea mail-ului către " + to);
        }
    }

    @Async
    public void sendBulkMatchEmail(List<String> userEmails, MatchNotificationRequestDTO details) {
        if (userEmails == null || userEmails.isEmpty()) {
            log.warn("Lista de email-uri este goală. Nicio notificare nu va fi trimisă.");
            return;
        }

        log.info(">>> Încep trimiterea asincronă pentru {} utilizatori", userEmails.size());

        LocalDateTime date = LocalDateTime.parse(details.getMatchDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, 'ora' HH:mm", new Locale("ro", "RO"));

        String formattedDate = date.format(formatter);

        String content = String.format(
                "<h2>Meci nou pe Arena!</h2>" +
                        "<p>Salutare!</p>" +
                        "<p>A fost adăugat un nou meci: <strong>Arena vs %s</strong></p>" +
                        "<p>Data: <strong>%s</strong></p>" +
                        "<p>Stadion: %s</p>" +
                        "<br><p>Te așteptăm pe stadion să ne susții echipa!</p>",
                details.getOpponentName(),
                formattedDate,
                details.getStadiumName()
        );

        for (String email : userEmails) {
            try {
                sendSimpleEmail(email, "Meci Nou: Arena vs " + details.getOpponentName(), content);
            } catch (Exception e) {

                log.error("Nu am putut trimite notificarea către: {} din cauza: {}", email, e.getMessage());
            }
        }
        log.info(">>> Toate notificările au fost procesate.");
    }
}