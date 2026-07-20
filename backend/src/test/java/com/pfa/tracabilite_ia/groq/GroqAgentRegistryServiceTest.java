package com.pfa.tracabilite_ia.groq;

import com.pfa.tracabilite_ia.ai.client.GroqClient;
import com.pfa.tracabilite_ia.config.GroqProperties;
import com.pfa.tracabilite_ia.dto.response.GroqStatusResponse;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroqAgentRegistryServiceTest {

    @Test
    void configuredAgents_exposesThreeGroqModels() {
        GroqProperties properties = new GroqProperties();
        properties.setApiKey("x");
        GroqClient client = mock(GroqClient.class);
        GroqAgentRegistryService registry = new GroqAgentRegistryService(properties, client);

        assertThat(registry.configuredAgents()).hasSize(3);
        assertThat(registry.configuredAgents().get(0).displayName()).isEqualTo("Llama 3.3 70B Versatile");
        assertThat(registry.configuredAgents().get(0).modelId()).isEqualTo("llama-3.3-70b-versatile");
        assertThat(registry.configuredAgents().get(1).modelId()).isEqualTo("openai/gpt-oss-120b");
        assertThat(registry.configuredAgents().get(2).modelId()).isEqualTo("openai/gpt-oss-20b");
        assertThat(registry.configuredAgents()).allMatch(a -> "GROQ".equals(a.provider()));
    }

    @Test
    void status_neverExposesApiKey() {
        GroqProperties properties = new GroqProperties();
        properties.setApiKey("secret-should-not-leak");
        GroqClient client = mock(GroqClient.class);
        when(client.listAvailableModelIds()).thenReturn(Set.of(
                "llama-3.3-70b-versatile", "openai/gpt-oss-120b", "openai/gpt-oss-20b"));

        GroqStatusResponse status = new GroqAgentRegistryService(properties, client).status();
        assertThat(status.isConfigured()).isTrue();
        assertThat(status.isReachable()).isTrue();
        assertThat(status.toString()).doesNotContain("secret-should-not-leak");
        assertThat(status.getModels()).hasSize(3);
    }
}
