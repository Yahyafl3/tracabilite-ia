package com.pfa.tracabilite_ia.openrouter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterAgentDisplayNamesTest {

    @Test
    void resolve_gemma26bA4b_matchesModelId() {
        String modelId = OpenRouterAgentRegistryService.MODEL_2_ID;

        assertThat(OpenRouterAgentDisplayNames.resolve(modelId)).isEqualTo("Gemma 4 26B A4B");
    }

    @Test
    void resolve_gemma31b_matchesModelId() {
        assertThat(OpenRouterAgentDisplayNames.resolve(OpenRouterAgentRegistryService.LEGACY_MODEL_2_ID))
                .isEqualTo("Gemma 4 31B");
    }

    @Test
    void resolve_neverMixes26bDisplayWith31bModelId() {
        String modelId26b = "google/gemma-4-26b-a4b-it:free";
        String modelId31b = "google/gemma-4-31b-it:free";

        assertThat(OpenRouterAgentDisplayNames.resolve(modelId26b)).isEqualTo("Gemma 4 26B A4B");
        assertThat(OpenRouterAgentDisplayNames.resolve(modelId26b)).isNotEqualTo("Gemma 4 31B");
        assertThat(OpenRouterAgentDisplayNames.resolve(modelId31b)).isEqualTo("Gemma 4 31B");
    }
}
