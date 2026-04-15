package com.arena.auth.service;


import org.springframework.mail.SimpleMailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Păstrăm doar metoda simplă pentru codul de verificare/welcome
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("[EMAIL AUTH] Mesaj trimis către: " + to);
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Eșec la trimitere: " + e.getMessage());
        }
    }
}