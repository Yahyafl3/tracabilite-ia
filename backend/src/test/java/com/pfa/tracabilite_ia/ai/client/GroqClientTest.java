package com.pfa.tracabilite_ia.ai.client;

import com.pfa.tracabilite_ia.ai.dto.GroqChatResult;
import com.pfa.tracabilite_ia.config.GroqProperties;
import com.pfa.tracabilite_ia.exception.GroqErrorCode;
import com.pfa.tracabilite_ia.exception.GroqException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroqClientTest {

    private MockWebServer mockWebServer;
    private GroqClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        GroqProperties properties = new GroqProperties();
        properties.setBaseUrl(mockWebServer.url("/openai/v1/").toString());
        properties.setApiKey("test-groq-key-not-real");
        properties.setMaxRetries(2);
        client = new GroqClient(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void chatCompletion_llama_returnsParsedResponse() throws InterruptedException {
        enqueueSuccess("llama-3.3-70b-versatile", 0.81);
        GroqChatResult result = client.chatCompletion(
                "llama-3.3-70b-versatile", "system", "user-prompt", "corr-llama");

        assertThat(result.getRequestedModelId()).isEqualTo("llama-3.3-70b-versatile");
        assertThat(result.getActualModelId()).isEqualTo("llama-3.3-70b-versatile");
        assertThat(result.getRawContent()).contains("APPROVE");
        assertThat(result.getTotalTokens()).isEqualTo(55);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/openai/v1/chat/completions");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-groq-key-not-real");
        assertThat(request.getHeader("X-Correlation-ID")).isEqualTo("corr-llama");
        String body = request.getBody().readUtf8();
        assertThat(body).contains("llama-3.3-70b-versatile");
        assertThat(body).doesNotContain("gsk_");
    }

    @Test
    void chatCompletion_gptOss120b() {
        enqueueSuccess("openai/gpt-oss-120b", 0.7);
        GroqChatResult result = client.chatCompletion(
                "openai/gpt-oss-120b", "system", "user", "corr-120");
        assertThat(result.getRequestedModelId()).isEqualTo("openai/gpt-oss-120b");
        assertThat(result.getActualModelId()).isEqualTo("openai/gpt-oss-120b");
    }

    @Test
    void chatCompletion_gptOss20b() {
        enqueueSuccess("openai/gpt-oss-20b", 0.65);
        GroqChatResult result = client.chatCompletion(
                "openai/gpt-oss-20b", "system", "user", "corr-20");
        assertThat(result.getRequestedModelId()).isEqualTo("openai/gpt-oss-20b");
    }

    @Test
    void chatCompletion_maps401() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        assertThatThrownBy(() -> client.chatCompletion("m", "s", "u", "c"))
                .isInstanceOf(GroqException.class)
                .extracting(ex -> ((GroqException) ex).getErrorCode())
                .isEqualTo(GroqErrorCode.GROQ_AUTHENTICATION_FAILED);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void chatCompletion_maps429ThenRetriesThenSucceeds_respectsRetryAfter() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", "0")
                .addHeader("x-ratelimit-remaining-requests", "0"));
        enqueueSuccess("llama-3.3-70b-versatile", 0.9);

        GroqChatResult result = client.chatCompletion("llama-3.3-70b-versatile", "s", "u", "retry");
        assertThat(result.getRetryCount()).isEqualTo(1);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        assertThat(mockWebServer.takeRequest().getPath()).contains("chat/completions");
    }

    @Test
    void chatCompletion_maps429Exhausted() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).addHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).addHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).addHeader("Retry-After", "0"));

        assertThatThrownBy(() -> client.chatCompletion("llama-3.3-70b-versatile", "s", "u", "c"))
                .isInstanceOf(GroqException.class)
                .extracting(ex -> ((GroqException) ex).getErrorCode())
                .isEqualTo(GroqErrorCode.GROQ_RATE_LIMITED);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
    }

    private void enqueueSuccess(String model, double confidence) {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "model": "%s",
                          "choices": [{
                            "message": {
                              "role": "assistant",
                              "content": "{\\"suggestedDecision\\":\\"APPROVE\\",\\"confidence\\":%s,\\"riskLevel\\":\\"LOW\\",\\"summary\\":\\"ok\\",\\"explanation\\":\\"detail\\",\\"recommendations\\":[\\"a\\"]}"
                            }
                          }],
                          "usage": {"prompt_tokens": 20, "completion_tokens": 35, "total_tokens": 55}
                        }
                        """.formatted(model, confidence))
                .addHeader("Content-Type", "application/json"));
    }
}
