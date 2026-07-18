package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.dto.response.OpenRouterModelStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenRouterAgentRegistryServiceTest {

    @Mock
    private OpenRouterModelsCacheService modelsCacheService;

    private OpenRouterProperties properties;
    private OpenRouterAgentRegistryService service;

    @BeforeEach
    void setUp() {
        properties = new OpenRouterProperties();
        properties.setApiKey("test-key-not-real");
        properties.setModel1(OpenRouterAgentRegistryService.MODEL_1_ID);
        properties.setModel2(OpenRouterAgentRegistryService.MODEL_2_ID);
        properties.setModel3(OpenRouterAgentRegistryService.MODEL_3_ID);
        service = new OpenRouterAgentRegistryService(properties, modelsCacheService);
    }

    @Test
    void configuredAgents_useExpectedModelsAndDisplayNames() {
        var agents = service.configuredAgents();

        assertThat(agents).hasSize(3);
        assertThat(agents.get(0).agentKey()).isEqualTo(OpenRouterAgentRegistryService.AGENT_1);
        assertThat(agents.get(0).displayName()).isEqualTo("Llama 3.3 70B");
        assertThat(agents.get(0).modelId()).isEqualTo(OpenRouterAgentRegistryService.MODEL_1_ID);
        assertThat(agents.get(0).provider()).isEqualTo("META_OPENROUTER");
        assertThat(agents.get(0).orderIndex()).isEqualTo(1);
        assertThat(agents.get(0).active()).isTrue();

        assertThat(agents.get(1).agentKey()).isEqualTo(OpenRouterAgentRegistryService.AGENT_2);
        assertThat(agents.get(1).displayName()).isEqualTo("Gemma 4 26B A4B");
        assertThat(agents.get(1).modelId()).isEqualTo(OpenRouterAgentRegistryService.MODEL_2_ID);
        assertThat(agents.get(1).displayName()).doesNotContain("31B");
        assertThat(agents.get(1).provider()).isEqualTo("GOOGLE_OPENROUTER");

        assertThat(agents.get(2).agentKey()).isEqualTo(OpenRouterAgentRegistryService.AGENT_3);
        assertThat(agents.get(2).displayName()).isEqualTo("GPT-OSS 20B");
        assertThat(agents.get(2).modelId()).isEqualTo(OpenRouterAgentRegistryService.MODEL_3_ID);
        assertThat(agents.get(2).provider()).isEqualTo("OPENAI_OPENROUTER");
        assertThat(agents.get(2).orderIndex()).isEqualTo(3);
    }

    @Test
    void configuredAgents_doNotUseLegacyGptOss120bModel() {
        assertThat(service.configuredAgents())
                .extracting(OpenRouterAgentDefinition::modelId)
                .doesNotContain(OpenRouterAgentRegistryService.LEGACY_MODEL_3_ID);

        assertThat(service.configuredAgents())
                .extracting(OpenRouterAgentDefinition::displayName)
                .doesNotContain("GPT-OSS 120B");
    }

    @Test
    void modelStatuses_marksListedModelsAsAvailable() {
        when(modelsCacheService.availableModelIds()).thenReturn(Set.of(
                OpenRouterAgentRegistryService.MODEL_1_ID,
                OpenRouterAgentRegistryService.MODEL_2_ID,
                OpenRouterAgentRegistryService.MODEL_3_ID
        ));

        var statuses = service.modelStatuses();

        assertThat(statuses).hasSize(3);
        assertThat(statuses)
                .extracting(OpenRouterModelStatusResponse::getModelId)
                .containsExactly(
                        OpenRouterAgentRegistryService.MODEL_1_ID,
                        OpenRouterAgentRegistryService.MODEL_2_ID,
                        OpenRouterAgentRegistryService.MODEL_3_ID
                );
        assertThat(statuses)
                .extracting(OpenRouterModelStatusResponse::getDisplayName)
                .containsExactly("Llama 3.3 70B", "Gemma 4 26B A4B", "GPT-OSS 20B");
        assertThat(statuses)
                .extracting(OpenRouterModelStatusResponse::getStatus)
                .containsOnly("AVAILABLE");
        assertThat(statuses)
                .filteredOn(status -> OpenRouterAgentRegistryService.AGENT_3.equals(status.getAgentKey()))
                .first()
                .satisfies(status -> {
                    assertThat(status.getModelId()).isEqualTo(OpenRouterAgentRegistryService.MODEL_3_ID);
                    assertThat(status.isAvailable()).isTrue();
                });
    }

    @Test
    void modelStatuses_marksMissingModelAsNotListed() {
        when(modelsCacheService.availableModelIds()).thenReturn(Set.of(
                OpenRouterAgentRegistryService.MODEL_1_ID,
                OpenRouterAgentRegistryService.MODEL_2_ID
        ));

        OpenRouterModelStatusResponse agent3 = service.modelStatuses().stream()
                .filter(status -> OpenRouterAgentRegistryService.AGENT_3.equals(status.getAgentKey()))
                .findFirst()
                .orElseThrow();

        assertThat(agent3.getModelId()).isEqualTo(OpenRouterAgentRegistryService.MODEL_3_ID);
        assertThat(agent3.getStatus()).isEqualTo("MODEL_NOT_LISTED");
        assertThat(agent3.isAvailable()).isFalse();
    }

    @Test
    void findAgent_returnsGptOss20bForAgent3() {
        OpenRouterAgentDefinition agent3 = service.findAgent(OpenRouterAgentRegistryService.AGENT_3);

        assertThat(agent3.displayName()).isEqualTo("GPT-OSS 20B");
        assertThat(agent3.modelId()).isEqualTo(OpenRouterAgentRegistryService.MODEL_3_ID);
    }
}
