package com.pfa.tracabilite_ia.mail;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class JdkResendHttpPoster implements ResendHttpPoster {

    private final HttpClient httpClient;
    private final Duration timeout;

    public JdkResendHttpPoster() {
        this(Duration.ofSeconds(10));
    }

    JdkResendHttpPoster(Duration timeout) {
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
    }

    @Override
    public HttpResult post(String url, Map<String, String> headers, String jsonBody)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "" : jsonBody));
        if (headers != null) {
            headers.forEach(builder::header);
        }
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpResult(response.statusCode(), response.body() == null ? "" : response.body());
    }
}
