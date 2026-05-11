package com.eventra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String toEmail, String fullName,
                                      String rawToken) {
        String link = "https://eventra.ro/verify-email?token=" + rawToken;
        String body = String.format(
                "Salut %s,%n%n" +
                        "Îți mulțumim că te-ai înregistrat pe Eventra!%n%n" +
                        "Verifică-ți adresa de email accesând link-ul:%n%s%n%n" +
                        "Link-ul expiră în 24 de ore.%n%n" +
                        "Dacă nu ai creat un cont, ignoră acest email.%n%n" +
                        "Echipa Eventra", fullName, link);

        send(toEmail, "Verifică-ți adresa de email — Eventra", body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName,
                                       String rawToken) {
        String link = "https://eventra.ro/reset-password?token=" + rawToken;
        String body = String.format(
                "Salut %s,%n%n" +
                        "Am primit o solicitare de resetare a parolei pentru contul tău.%n%n" +
                        "Resetează parola accesând link-ul:%n%s%n%n" +
                        "Link-ul expiră în 1 oră.%n%n" +
                        "Dacă nu ai solicitat resetarea, ignoră acest email.%n%n" +
                        "Echipa Eventra", fullName, link);

        send(toEmail, "Resetează parola — Eventra", body);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String body = String.format(
                "Salut %s,%n%n" +
                        "Contul tău Eventra a fost activat cu succes!%n%n" +
                        "Începe să planifici: https://eventra.ro%n%n" +
                        "Echipa Eventra", fullName);

        send(toEmail, "Bun venit pe Eventra!", body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("noreply@eventra.ro");
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email trimis la: {}", to);
        } catch (Exception ex) {
            log.error("Eroare trimitere email către {}: {}", to, ex.getMessage());
        }
    }
}