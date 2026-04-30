package com.arena.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Service
@RequiredArgsConstructor
@RefreshScope
public class EmailService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JavaMailSender mailSender;

    public void sendTicketWithAttachment(String toEmail, String subject, byte[] pdfBytes, String fileName) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        // 'true' indică faptul că mesajul este multipart (conține atașament)
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText("<p>Salutare!</p><p>Atașat găsești biletul tău pentru meci. Ne vedem pe stadion!</p>", true);
        helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true pentru HTML
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Eșec la trimiterea mail-ului simplu către " + to, e);
        }
    }
}