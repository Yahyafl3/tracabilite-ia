package com.pfa.tracabilite_ia.ai.client;

import com.pfa.tracabilite_ia.ai.dto.OpenRouterChatRequest;
import com.pfa.tracabilite_ia.ai.dto.OpenRouterChatResponse;
import com.pfa.tracabilite_ia.ai.dto.OpenRouterChatResult;
import com.pfa.tracabilite_ia.ai.dto.OpenRouterKeyResponse;
import com.pfa.tracabilite_ia.ai.dto.OpenRouterModelsResponse;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.dto.response.OpenRouterKeyStatusResponse;
import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import com.pfa.tracabilite_ia.openrouter.OpenRouterRetryPolicy;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OpenRouterClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterClient.class);

    private final OpenRouterProperties properties;
    private final RestClient restClient;

    public OpenRouterClient(OpenRouterProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .requestFactory(createRequestFactory(properties))
                .build();
    }

    public OpenRouterChatResult chatCompletion(String modelId,
                                               String systemPrompt,
                                               String userPrompt,
                                               String correlationId) {
        return chatCompletionWithModels(List.of(modelId), modelId, systemPrompt, userPrompt, correlationId);
    }

    public OpenRouterChatResult chatCompletionWithModels(List<String> models,
                                                         String requestedModelId,
                                                         String systemPrompt,
                                                         String userPrompt,
                                                         String correlationId) {
        ensureConfigured();
        if (models == null || models.isEmpty()) {
            throw new OpenRouterException(OpenRouterErrorCode.MODEL_UNAVAILABLE, "Aucun modele OpenRouter disponible");
        }

        int retriesPerformed = 0;
        OpenRouterException lastRetryable = null;

        for (int attempt = 0; attempt <= OpenRouterRetryPolicy.MAX_RETRIES; attempt++) {
            if (attempt > 0) {
                long delayMs = OpenRouterRetryPolicy.delayBeforeRetry(attempt - 1,
                        lastRetryable != null ? lastRetryable.getResponseHeaders() : null);
                log.info("Retry OpenRouter attempt={} delayMs={} models={}", attempt, delayMs, models);
                sleepQuietly(delayMs);
                retriesPerformed++;
            }

            try {
                return executeChatCompletion(models, requestedModelId, systemPrompt, userPrompt, correlationId, retriesPerformed);
            } catch (OpenRouterException ex) {
                if (!OpenRouterRetryPolicy.isRetryable(ex) || attempt >= OpenRouterRetryPolicy.MAX_RETRIES) {
                    throw ex;
                }
                lastRetryable = ex;
                log.warn("Erreur OpenRouter temporaire {} — retry {}/{}",
                        ex.getErrorCode(), attempt + 1, OpenRouterRetryPolicy.MAX_RETRIES);
            }
        }

        throw lastRetryable != null ? lastRetryable
                : new OpenRouterException(OpenRouterErrorCode.OPENROUTER_UNAVAILABLE, "Echec OpenRouter");
    }

    private OpenRouterChatResult executeChatCompletion(List<String> models,
                                                         String requestedModelId,
                                                         String systemPrompt,
                                                         String userPrompt,
                                                         String correlationId,
                                                         int retryCount) {
        OpenRouterChatRequest request = new OpenRouterChatRequest(
                models.get(0),
                models,
                List.of(
                        new OpenRouterChatRequest.Message("system", systemPrompt),
                        new OpenRouterChatRequest.Message("user", userPrompt)
                ),
                new OpenRouterChatRequest.ResponseFormat("json_object")
        );

        long startedAt = System.currentTimeMillis();
        try {
            log.info("Appel OpenRouter models={} requestedModel={} correlationId={}", models, requestedModelId, correlationId);
            OpenRouterChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .headers(headers -> applyHeaders(headers, correlationId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OpenRouterChatResponse.class);

            String rawContent = extractContent(response);
            String actualModelId = response != null && response.getModel() != null
                    ? response.getModel()
                    : models.get(0);

            OpenRouterChatResult result = new OpenRouterChatResult();
            result.setResponse(response);
            result.setRawContent(rawContent);
            result.setDurationMs(System.currentTimeMillis() - startedAt);
            result.setRetryCount(retryCount);
            result.setRequestedModelId(requestedModelId);
            result.setActualModelId(actualModelId);
            result.setModelsRequested(models);
            result.setResponseHash(HashUtils.sha256(rawContent));
            result.setFallbackUsed(!Objects.equals(requestedModelId, actualModelId));
            if (result.isFallbackUsed()) {
                result.setFallbackReason("MODEL_FALLBACK");
            }
            return result;
        } catch (ResourceAccessException ex) {
            throw new OpenRouterException(
                    OpenRouterErrorCode.OPENROUTER_TIMEOUT,
                    "OpenRouter timeout ou reseau indisponible",
                    0,
                    ex,
                    null
            );
        } catch (RestClientResponseException ex) {
            throw mapHttpError(ex);
        }
    }

    public Set<String> listAvailableModelIds() {
        ensureConfigured();
        try {
            OpenRouterModelsResponse response = restClient.get()
                    .uri("/models")
                    .headers(headers -> applyHeaders(headers, "models-status"))
                    .retrieve()
                    .body(OpenRouterModelsResponse.class);
            if (response == null || response.getData() == null) {
                return Set.of();
            }
            return response.getData().stream()
                    .map(OpenRouterModelsResponse.ModelData::getId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (ResourceAccessException ex) {
            throw new OpenRouterException(
                    OpenRouterErrorCode.OPENROUTER_TIMEOUT,
                    "Impossible de recuperer la liste des modeles OpenRouter",
                    0,
                    ex,
                    null
            );
        } catch (RestClientResponseException ex) {
            throw mapHttpError(ex);
        }
    }

    public OpenRouterKeyStatusResponse fetchKeyStatus() {
        ensureConfigured();
        try {
            OpenRouterKeyResponse response = restClient.get()
                    .uri("/key")
                    .headers(headers -> applyHeaders(headers, "key-status"))
                    .retrieve()
                    .body(OpenRouterKeyResponse.class);

            OpenRouterKeyResponse.KeyData data = response != null ? response.getData() : null;
            double usage = data != null && data.getUsage() != null ? data.getUsage() : 0d;
            Double limit = data != null ? data.getLimit() : null;
            boolean freeTier = data != null && Boolean.TRUE.equals(data.getFreeTier());

            Double remaining = null;
            boolean available = true;
            if (limit != null && limit > 0) {
                remaining = Math.max(0d, limit - usage);
                available = remaining >= 1;
            }

            return OpenRouterKeyStatusResponse.builder()
                    .freeTier(freeTier)
                    .dailyUsage(usage)
                    .remainingLimit(remaining)
                    .available(available)
                    .build();
        } catch (ResourceAccessException ex) {
            return OpenRouterKeyStatusResponse.builder()
                    .freeTier(false)
                    .dailyUsage(0)
                    .remainingLimit(0d)
                    .available(false)
                    .message("OpenRouter indisponible")
                    .build();
        } catch (RestClientResponseException ex) {
            OpenRouterException mapped = mapHttpError(ex);
            return OpenRouterKeyStatusResponse.builder()
                    .freeTier(false)
                    .dailyUsage(0)
                    .remainingLimit(0d)
                    .available(false)
                    .message(mapped.getMessage())
                    .build();
        }
    }

    private void applyHeaders(HttpHeaders headers, String correlationId) {
        headers.setBearerAuth(properties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("HTTP-Referer", properties.getHttpReferer());
        headers.set("X-Title", properties.getAppTitle());
        if (correlationId != null && !correlationId.isBlank()) {
            headers.set("X-Correlation-ID", correlationId);
        }
    }

    private OpenRouterException mapHttpError(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        OpenRouterErrorCode code = switch (status) {
            case 401 -> OpenRouterErrorCode.OPENROUTER_AUTHENTICATION_FAILED;
            case 402 -> OpenRouterErrorCode.OPENROUTER_CREDIT_ERROR;
            case 404 -> OpenRouterErrorCode.OPENROUTER_MODEL_NOT_FOUND;
            case 429 -> OpenRouterErrorCode.OPENROUTER_RATE_LIMITED;
            default -> status >= 500
                    ? OpenRouterErrorCode.OPENROUTER_UNAVAILABLE
                    : OpenRouterErrorCode.OPENROUTER_UNAVAILABLE;
        };
        log.warn("Erreur OpenRouter HTTP {} code={}", status, code);
        return new OpenRouterException(code, "Erreur OpenRouter HTTP " + status, status, ex, ex.getResponseHeaders());
    }

    private String extractContent(OpenRouterChatResponse response) {
        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()
                || response.getChoices().get(0).getMessage() == null
                || response.getChoices().get(0).getMessage().getContent() == null) {
            throw new OpenRouterException(
                    OpenRouterErrorCode.OPENROUTER_INVALID_RESPONSE,
                    "Reponse OpenRouter vide"
            );
        }
        return response.getChoices().get(0).getMessage().getContent().trim();
    }

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new OpenRouterException(
                    OpenRouterErrorCode.OPENROUTER_AUTHENTICATION_FAILED,
                    "OPENROUTER_API_KEY non configuree"
            );
        }
    }

    private static void sleepQuietly(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://openrouter.ai/api/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private static SimpleClientHttpRequestFactory createRequestFactory(OpenRouterProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return factory;
    }
}
