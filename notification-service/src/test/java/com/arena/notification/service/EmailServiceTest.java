package com.arena.notification.service;

import com.arena.notification.dto.external.MatchNotificationRequestDTO;
import com.arena.notification.exception.NotificationException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private MatchNotificationRequestDTO matchDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@arena.ro");
        // Folosim formatul ISO standard pe care metoda parse() îl așteaptă în codul tău
        String isoDateTime = LocalDateTime.now().plusDays(3).withNano(0).toString();

        matchDetails = new MatchNotificationRequestDTO(
                "FCSB",
                isoDateTime,
                "Arena Nationala",
                "http://arena.ro/match/10"
        );
    }

    @Test
    @DisplayName("Send Ticket With Attachment - Succes cu atașamente multiple (PDF-uri)")
    void sendTicketWithAttachment_Success() throws Exception {
        // Arrange
        String toEmail = "suporter@arena.ro";
        String subject = "Biletele tale pentru meci";
        List<byte[]> pdfs = List.of(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        List<String> fileNames = List.of("bilet1.pdf", "bilet2.pdf");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendTicketWithAttachment(toEmail, subject, pdfs, fileNames);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Simple Email - Succes")
    void sendSimpleEmail_Success() {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendSimpleEmail("test@arena.ro", "Subiect", "Continut HTML");

        // Assert
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Simple Email - Aruncă excepție personalizată dacă trimiterea eșuează")
    void sendSimpleEmail_Failure_ThrowsNotificationException() {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThatThrownBy(() -> emailService.sendSimpleEmail("test@arena.ro", "Subiect", "Continut"))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("Eșec la trimiterea mail-ului către");
    }

    @Test
    @DisplayName("Send Bulk Match Email - Succes trimitere în masă utilizatori")
    void sendBulkMatchEmail_Success() {
        // Arrange
        List<String> emails = List.of("user1@arena.ro", "user2@arena.ro");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendBulkMatchEmail(emails, matchDetails);

        // Assert
        // Verificăm că send() s-a apelat de 2 ori (o dată pentru fiecare email din listă)
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    @DisplayName("Send Bulk Match Email - Se oprește imediat dacă lista de email-uri este goală")
    void sendBulkMatchEmail_EmptyList_Ignored() {
        // Act
        emailService.sendBulkMatchEmail(Collections.emptyList(), matchDetails);

        // Assert
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}