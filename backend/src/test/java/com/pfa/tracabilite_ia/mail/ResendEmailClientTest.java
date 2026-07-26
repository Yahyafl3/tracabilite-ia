package com.pfa.tracabilite_ia.mail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResendEmailClientTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void attachLogAppender() {
        logger = (Logger) LoggerFactory.getLogger(ResendEmailClient.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void detachLogAppender() {
        logger.detachAppender(appender);
    }

    @Test
    void sendHtml_successOn2xx() {
        AtomicReference<String> body = new AtomicReference<>();
        ResendHttpPoster poster = (url, headers, jsonBody) -> {
            assertThat(url).isEqualTo("https://api.resend.com/emails");
            assertThat(headers.get("Authorization")).isEqualTo("Bearer re_test_key");
            body.set(jsonBody);
            return new ResendHttpPoster.HttpResult(200, "{\"id\":\"abc\"}");
        };

        ResendEmailClient client = new ResendEmailClient(
                poster,
                new ObjectMapper(),
                "https://api.resend.com/emails",
                "re_test_key"
        );

        client.sendHtml(
                "Traçabilité IA <noreply@example.com>",
                "user@test.fr",
                "Réinitialisation",
                "<a href=\"https://tracabilite-ia.vercel.app/auth/reset-password?token=secret-token\">Reset</a>"
        );

        assertThat(body.get())
                .contains("user@test.fr")
                .contains("tracabilite-ia.vercel.app/auth/reset-password")
                .contains("secret-token");
        assertThat(joinedLogs()).doesNotContain("re_test_key");
        assertThat(joinedLogs()).doesNotContain("secret-token");
    }

    @Test
    void sendHtml_non2xx_throwsAndDoesNotLeakSecrets() {
        ResendHttpPoster poster = (url, headers, jsonBody) ->
                new ResendHttpPoster.HttpResult(401, "{\"message\":\"Invalid API key re_leaked_key\"}");

        ResendEmailClient client = new ResendEmailClient(
                poster,
                new ObjectMapper(),
                "https://api.resend.com/emails",
                "re_secret_api_key"
        );

        assertThatThrownBy(() -> client.sendHtml("from@test.fr", "to@test.fr", "Sujet", "<p>x</p>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Resend HTTP 401");

        String logs = joinedLogs();
        assertThat(logs).contains("non-2xx");
        assertThat(logs).doesNotContain("re_secret_api_key");
        assertThat(logs).doesNotContain("re_leaked_key");
    }

    @Test
    void sendHtml_timeout_throwsWithoutSecrets() {
        ResendHttpPoster poster = (url, headers, jsonBody) -> {
            throw new HttpTimeoutException("timed out");
        };

        ResendEmailClient client = new ResendEmailClient(
                poster,
                new ObjectMapper(),
                "https://api.resend.com/emails",
                "re_timeout_key"
        );

        assertThatThrownBy(() -> client.sendHtml("from@test.fr", "to@test.fr", "Sujet", "<p>x</p>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("timeout");

        assertThat(joinedLogs()).doesNotContain("re_timeout_key");
    }

    @Test
    void sanitizeBody_redactsApiKeysAndEmails() {
        String sanitized = ResendEmailClient.sanitizeBody(
                "error bearer re_abc123 for user admin@tracabilite.ia token=supersecret"
        );
        assertThat(sanitized).doesNotContain("re_abc123");
        assertThat(sanitized).doesNotContain("admin@tracabilite.ia");
        assertThat(sanitized).doesNotContain("supersecret");
    }

    private String joinedLogs() {
        List<ILoggingEvent> events = appender.list;
        StringBuilder sb = new StringBuilder();
        for (ILoggingEvent event : events) {
            sb.append(event.getFormattedMessage()).append('\n');
        }
        return sb.toString();
    }
}
