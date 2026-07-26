package com.pfa.tracabilite_ia.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends transactional email via Resend HTTPS API (no SMTP).
 */
@Component
public class ResendEmailClient {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailClient.class);

    private final ResendHttpPoster httpPoster;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;

    public ResendEmailClient(
            ResendHttpPoster httpPoster,
            ObjectMapper objectMapper,
            @Value("${app.mail.resend.api-url:https://api.resend.com/emails}") String apiUrl,
            @Value("${app.mail.resend.api-key:}") String apiKey
    ) {
        this.httpPoster = httpPoster;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public void sendHtml(String from, String to, String subject, String html) {
        if (apiKey.isBlank()) {
            log.error("Resend email aborted: RESEND_API_KEY not configured");
            throw new IllegalStateException("Impossible d'envoyer l'email (provider Resend non configure).");
        }
        if (from == null || from.isBlank()) {
            log.error("Resend email aborted: MAIL_FROM not configured");
            throw new IllegalStateException("Impossible d'envoyer l'email (expediteur non configure).");
        }

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("from", from);
            payload.put("to", List.of(to));
            payload.put("subject", subject);
            payload.put("html", html);

            String json = objectMapper.writeValueAsString(payload);
            Map<String, String> headers = Map.of(
                    "Authorization", "Bearer " + apiKey,
                    "Content-Type", "application/json"
            );

            ResendHttpPoster.HttpResult result = httpPoster.post(apiUrl, headers, json);
            if (result.statusCode() < 200 || result.statusCode() >= 300) {
                log.error(
                        "Resend API returned non-2xx status={} bodySnippet={}",
                        result.statusCode(),
                        sanitizeBody(result.body())
                );
                throw new IllegalStateException("Impossible d'envoyer l'email (Resend HTTP "
                        + result.statusCode() + ").");
            }
            log.info("Resend email accepted status={}", result.statusCode());
        } catch (HttpTimeoutException ex) {
            log.error("Resend API timeout errorType={}", ex.getClass().getSimpleName());
            throw new IllegalStateException("Impossible d'envoyer l'email (timeout Resend).");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Resend API interrupted");
            throw new IllegalStateException("Impossible d'envoyer l'email (interrompu).");
        } catch (IOException ex) {
            log.error("Resend API IO failure errorType={}", ex.getClass().getSimpleName());
            throw new IllegalStateException("Impossible d'envoyer l'email (erreur reseau Resend).");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Resend API unexpected failure errorType={}", ex.getClass().getSimpleName());
            throw new IllegalStateException("Impossible d'envoyer l'email.");
        }
    }

    static String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String cleaned = body
                .replaceAll("(?i)(password|passwd|secret|token|authorization|bearer|api[_-]?key)\\s*[=:].*", "$1=***")
                .replaceAll("(?i)re_[A-Za-z0-9_]+", "re_***")
                .replaceAll("[\\w.+-]+@[\\w.-]+", "[redacted]");
        return cleaned.length() > 180 ? cleaned.substring(0, 180) + "…" : cleaned;
    }
}
