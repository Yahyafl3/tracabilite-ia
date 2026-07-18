package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.ai.client.OpenRouterClient;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.dto.response.OpenRouterKeyStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenRouterKeyStatusServiceTest {

    @Mock
    private OpenRouterClient openRouterClient;

    private OpenRouterProperties properties;
    private OpenRouterKeyStatusService service;

    @BeforeEach
    void setUp() {
        properties = new OpenRouterProperties();
        properties.setApiKey("test-key-not-real");
        service = new OpenRouterKeyStatusService(openRouterClient, properties);
    }

    @Test
    void hasQuotaForAgents_returnsFalseWhenRemainingLimitTooLow() {
        when(openRouterClient.fetchKeyStatus()).thenReturn(OpenRouterKeyStatusResponse.builder()
                .freeTier(true)
                .dailyUsage(9)
                .remainingLimit(2d)
                .available(true)
                .build());

        assertThat(service.hasQuotaForAgents(3)).isFalse();
    }

    @Test
    void hasQuotaForAgents_returnsTrueWhenRemainingLimitIsEnough() {
        when(openRouterClient.fetchKeyStatus()).thenReturn(OpenRouterKeyStatusResponse.builder()
                .freeTier(true)
                .dailyUsage(1)
                .remainingLimit(5d)
                .available(true)
                .build());

        assertThat(service.hasQuotaForAgents(3)).isTrue();
    }

    @Test
    void fetchStatus_returnsUnavailableWhenKeyMissing() {
        properties.setApiKey("");
        OpenRouterKeyStatusResponse status = service.fetchStatus();

        assertThat(status.isAvailable()).isFalse();
        assertThat(status.getMessage()).contains("non configuree");
    }
}
