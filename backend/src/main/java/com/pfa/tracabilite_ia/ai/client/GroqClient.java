package com.pfa.tracabilite_ia.ai.client;

import com.pfa.tracabilite_ia.ai.dto.GroqChatResult;
import com.pfa.tracabilite_ia.ai.dto.GroqChoice;
import com.pfa.tracabilite_ia.ai.dto.GroqMessage;
import com.pfa.tracabilite_ia.ai.dto.GroqRequest;
import com.pfa.tracabilite_ia.ai.dto.GroqResponse;
import com.pfa.tracabilite_ia.ai.dto.GroqUsage;
import com.pfa.tracabilite_ia.config.GroqProperties;
import com.pfa.tracabilite_ia.exception.GroqErrorCode;
import com.pfa.tracabilite_ia.exception.GroqException;
import com.pfa.tracabilite_ia.groq.GroqRetryPolicy;
import com.pfa.tracabilite_ia.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);

    private final GroqProperties properties;
    private final RestClient restClient;

    public GroqClient(GroqProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .requestFactory(createRequestFactory(properties))
                .build();
    }

    public GroqChatResult chatCompletion(String modelId,
                                         String systemPrompt,
                                         String userPrompt,
                                         String correlationId) {
        ensureConfigured();
        if (modelId == null || modelId.isBlank()) {
            throw new GroqException(GroqErrorCode.MODEL_UNAVAILABLE, "Aucun modele Groq configure");
        }

        int maxRetries = Math.max(0, properties.getMaxRetries());
        int retriesPerformed = 0;
        GroqException lastRetryable = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (attempt > 0) {
                long delayMs = GroqRetryPolicy.delayBeforeRetry(attempt - 1,
                        lastRetryable != null ? lastRetryable.getResponseHeaders() : null);
                log.info("Retry Groq attempt={} delayMs={} model={}", attempt, delayMs, modelId);
                sleepQuietly(delayMs);
                retriesPerformed++;
            }

            try {
                return executeChatCompletion(modelId, systemPrompt, userPrompt, correlationId, retriesPerformed);
            } catch (GroqException ex) {
                if (!GroqRetryPolicy.isRetryable(ex) || attempt >= maxRetries) {
                    throw ex;
                }
                lastRetryable = ex;
                log.warn("Erreur Groq temporaire {} — retry {}/{}",
                        ex.getErrorCode(), attempt + 1, maxRetries);
            }
        }

        throw lastRetryable != null ? lastRetryable
                : new GroqException(GroqErrorCode.GROQ_UNAVAILABLE, "Echec Groq");
    }

    public Set<String> listAvailableModelIds() {
        ensureConfigured();
        try {
            GroqModelsListResponse response = restClient.get()
                    .uri("/models")
                    .headers(headers -> applyHeaders(headers, "models-status"))
                    .retrieve()
                    .body(GroqModelsListResponse.class);
            if (response == null || response.getData() == null) {
                return Set.of();
            }
            return response.getData().stream()
                    .map(GroqModelsListResponse.ModelData::getId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (ResourceAccessException ex) {
            throw new GroqException(GroqErrorCode.GROQ_TIMEOUT,
                    "Impossible de recuperer la liste des modeles Groq", 0, ex);
        } catch (RestClientResponseException ex) {
            throw mapHttpError(ex);
        }
    }

    public boolean ping() {
        if (!properties.isConfigured()) {
            return false;
        }
        try {
            listAvailableModelIds();
            return true;
        } catch (Exception ex) {
            log.warn("Groq ping echoue: {}", ex.getMessage());
            return false;
        }
    }

    private GroqChatResult executeChatCompletion(String modelId,
                                                 String systemPrompt,
                                                 String userPrompt,
                                                 String correlationId,
                                                 int retryCount) {
        GroqRequest request = new GroqRequest(
                modelId,
                List.of(
                        new GroqMessage("system", systemPrompt),
                        new GroqMessage("user", userPrompt)
                ),
                new GroqRequest.ResponseFormat("json_object")
        );

        long startedAt = System.currentTimeMillis();
        try {
            log.info("Appel Groq model={} correlationId={}", modelId, correlationId);
            GroqResponse response = restClient.post()
                    .uri("/chat/completions")
                    .headers(headers -> applyHeaders(headers, correlationId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            String rawContent = extractContent(response);
            String actualModelId = response != null && response.getModel() != null
                    ? response.getModel()
                    : modelId;

            GroqUsage usage = response != null ? response.getUsage() : null;
            GroqChatResult result = new GroqChatResult();
            result.setResponse(response);
            result.setRawContent(rawContent);
            result.setDurationMs(System.currentTimeMillis() - startedAt);
            result.setRetryCount(retryCount);
            result.setRequestedModelId(modelId);
            result.setActualModelId(actualModelId);
            result.setResponseHash(HashUtils.sha256(rawContent));
            if (usage != null) {
                result.setPromptTokens(usage.getPromptTokens());
                result.setCompletionTokens(usage.getCompletionTokens());
                result.setTotalTokens(usage.getTotalTokens());
            }
            return result;
        } catch (ResourceAccessException ex) {
            throw new GroqException(GroqErrorCode.GROQ_TIMEOUT,
                    "Groq timeout ou reseau indisponible", 0, ex, null);
        } catch (RestClientResponseException ex) {
            throw mapHttpError(ex);
        }
    }

    private void applyHeaders(HttpHeaders headers, String correlationId) {
        headers.setBearerAuth(properties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", correlationId != null ? correlationId : "unknown");
    }

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new GroqException(GroqErrorCode.GROQ_AUTHENTICATION_FAILED,
                    "GROQ_API_KEY non configuree");
        }
    }

    private String extractContent(GroqResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new GroqException(GroqErrorCode.GROQ_INVALID_RESPONSE, "Reponse Groq vide");
        }
        GroqChoice choice = response.getChoices().get(0);
        if (choice == null || choice.getMessage() == null || choice.getMessage().getContent() == null) {
            throw new GroqException(GroqErrorCode.GROQ_INVALID_RESPONSE, "Contenu Groq manquant");
        }
        return choice.getMessage().getContent();
    }

    private GroqException mapHttpError(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        HttpHeaders headers = ex.getResponseHeaders();
        String body = safeBody(ex);

        // Never log API key; body may be logged at warn without secrets
        log.warn("Erreur HTTP Groq status={} bodySnippet={}", status, truncate(body, 200));

        return switch (status) {
            case 401, 403 -> new GroqException(GroqErrorCode.GROQ_AUTHENTICATION_FAILED,
                    "Authentification Groq echouee", status, ex, headers);
            case 404 -> new GroqException(GroqErrorCode.GROQ_MODEL_NOT_FOUND,
                    "Modele Groq introuvable", status, ex, headers);
            case 429 -> new GroqException(GroqErrorCode.GROQ_RATE_LIMITED,
                    "Quota Groq temporairement atteint", status, ex, headers);
            case 408, 504 -> new GroqException(GroqErrorCode.GROQ_TIMEOUT,
                    "Timeout Groq", status, ex, headers);
            default -> {
                if (status >= 500) {
                    yield new GroqException(GroqErrorCode.GROQ_UNAVAILABLE,
                            "Service Groq indisponible", status, ex, headers);
                }
                yield new GroqException(GroqErrorCode.GROQ_UNAVAILABLE,
                        "Erreur Groq HTTP " + status, status, ex, headers);
            }
        };
    }

    private static String safeBody(RestClientResponseException ex) {
        try {
            return ex.getResponseBodyAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "…";
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.groq.com/openai/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private static SimpleClientHttpRequestFactory createRequestFactory(GroqProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return factory;
    }

    private static void sleepQuietly(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /** Minimal DTO for GET /models. */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroqModelsListResponse {
        private List<ModelData> data;

        public List<ModelData> getData() {
            return data;
        }

        public void setData(List<ModelData> data) {
            this.data = data;
        }

        @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
        public static class ModelData {
            private String id;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }
        }
    }
}
