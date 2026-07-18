package com.pfa.tracabilite_ia.ai.client;

import com.pfa.tracabilite_ia.ai.dto.OpenRouterChatResult;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenRouterClientTest {

    private MockWebServer mockWebServer;
    private OpenRouterClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        OpenRouterProperties properties = new OpenRouterProperties();
        properties.setBaseUrl(mockWebServer.url("/api/v1/").toString());
        properties.setApiKey("test-key-not-real");
        properties.setHttpReferer("http://localhost");
        properties.setAppTitle("Tracabilite-IA");
        client = new OpenRouterClient(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void chatCompletion_returnsParsedResponse() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "model": "meta-llama/llama-3.3-70b-instruct:free",
                          "choices": [{
                            "message": {
                              "role": "assistant",
                              "content": "{\\"suggestedDecision\\":\\"APPROUVER\\",\\"confidence\\":0.8,\\"riskLevel\\":\\"LOW\\",\\"summary\\":\\"ok\\",\\"explanation\\":\\"test\\",\\"recommendations\\":[\\"a\\"]}"
                            }
                          }],
                          "usage": {"total_tokens": 42}
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        OpenRouterChatResult result = client.chatCompletion(
                "meta-llama/llama-3.3-70b-instruct:free",
                "system",
                "user",
                "corr-1"
        );

        assertThat(result.getRawContent()).contains("APPROUVER");
        assertThat(result.getTotalTokens()).isEqualTo(42);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/v1/chat/completions");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-key-not-real");
        assertThat(request.getHeader("HTTP-Referer")).isEqualTo("http://localhost");
        assertThat(request.getHeader("X-Title")).isEqualTo("Tracabilite-IA");
        assertThat(request.getHeader("X-Correlation-ID")).isEqualTo("corr-1");
        assertThat(request.getBody().readUtf8()).contains("meta-llama/llama-3.3-70b-instruct:free");
    }

    @Test
    void chatCompletion_maps401ToAuthenticationFailed() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        assertThatThrownBy(() -> client.chatCompletion("model", "system", "user", "corr-2"))
                .isInstanceOf(OpenRouterException.class)
                .extracting(ex -> ((OpenRouterException) ex).getErrorCode())
                .isEqualTo(OpenRouterErrorCode.OPENROUTER_AUTHENTICATION_FAILED);

        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void chatCompletion_maps429ToRateLimitedAfterRetries() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));

        assertThatThrownBy(() -> client.chatCompletion("openai/gpt-oss-20b:free", "system", "user", "corr-429"))
                .isInstanceOf(OpenRouterException.class)
                .extracting(ex -> ((OpenRouterException) ex).getErrorCode())
                .isEqualTo(OpenRouterErrorCode.OPENROUTER_RATE_LIMITED);

        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
    }

    @Test
    void chatCompletionWithModels_recordsActualModelWhenFallbackUsed() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "model": "google/gemma-4-26b-a4b-it:free",
                          "choices": [{
                            "message": {
                              "role": "assistant",
                              "content": "{\\"suggestedDecision\\":\\"APPROUVER\\",\\"confidence\\":0.7,\\"riskLevel\\":\\"LOW\\",\\"summary\\":\\"ok\\",\\"explanation\\":\\"fallback\\",\\"recommendations\\":[]}"
                            }
                          }],
                          "usage": {"total_tokens": 10}
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        OpenRouterChatResult result = client.chatCompletionWithModels(
                List.of("meta-llama/llama-3.3-70b-instruct:free", "google/gemma-4-26b-a4b-it:free"),
                "meta-llama/llama-3.3-70b-instruct:free",
                "system",
                "user",
                "corr-fallback"
        );

        assertThat(result.getRequestedModelId()).isEqualTo("meta-llama/llama-3.3-70b-instruct:free");
        assertThat(result.getActualModelId()).isEqualTo("google/gemma-4-26b-a4b-it:free");
        assertThat(result.isFallbackUsed()).isTrue();
        assertThat(result.getResponseHash()).isNotBlank();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getBody().readUtf8()).contains("\"models\"");
    }

    @Test
    void listAvailableModelIds_returnsIds() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "data": [
                            {"id": "meta-llama/llama-3.3-70b-instruct:free"},
                            {"id": "google/gemma-4-31b-it:free"},
                            {"id": "openai/gpt-oss-20b:free"}
                          ]
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        assertThat(client.listAvailableModelIds())
                .contains(
                        "meta-llama/llama-3.3-70b-instruct:free",
                        "google/gemma-4-31b-it:free",
                        "openai/gpt-oss-20b:free")
                .doesNotContain("openai/gpt-oss-120b:free");
    }
}
