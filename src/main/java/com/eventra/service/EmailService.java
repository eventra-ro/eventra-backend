package com.eventra.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestClient restClient;
    private final String apiKey;

    public EmailService(@Value("${resend.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Async
    public void sendVerificationEmail(String toEmail, String fullName,
                                      String rawToken) {
        String link = "https://eventra.ro/verify-email?token=" + rawToken;
        String body = String.format(
                "Salut %s,\n\n" +
                        "Îți mulțumim că te-ai înregistrat pe Eventra!\n\n" +
                        "Verifică-ți adresa de email accesând link-ul:\n%s\n\n" +
                        "Link-ul expiră în 24 de ore.\n\n" +
                        "Dacă nu ai creat un cont, ignoră acest email.\n\n" +
                        "Echipa Eventra", fullName, link);

        send(toEmail, "Verifică-ți adresa de email — Eventra", body);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName,
                                       String rawToken) {
        String link = "https://eventra.ro/reset-password?token=" + rawToken;
        String body = String.format(
                "Salut %s,\n\n" +
                        "Am primit o solicitare de resetare a parolei pentru contul tău.\n\n" +
                        "Resetează parola accesând link-ul:\n%s\n\n" +
                        "Link-ul expiră în 1 oră.\n\n" +
                        "Dacă nu ai solicitat resetarea, ignoră acest email.\n\n" +
                        "Echipa Eventra", fullName, link);

        send(toEmail, "Resetează parola — Eventra", body);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String body = String.format(
                "Salut %s,\n\n" +
                        "Contul tău Eventra a fost activat cu succes!\n\n" +
                        "Începe să planifici: https://eventra.ro\n\n" +
                        "Echipa Eventra", fullName);

        send(toEmail, "Bun venit pe Eventra!", body);
    }

    private void send(String to, String subject, String text) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("RESEND_API_KEY nu este configurat — email nesitrimis la: {}",
                    to);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "from", "Eventra <noreply@eventra.ro>",
                    "to", List.of(to),
                    "subject", subject,
                    "text", text
            );

            restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email trimis via Resend API la: {}", to);
        } catch (Exception ex) {
            log.error("Eroare trimitere email la {}: {}", to, ex.getMessage());
        }
    }
}