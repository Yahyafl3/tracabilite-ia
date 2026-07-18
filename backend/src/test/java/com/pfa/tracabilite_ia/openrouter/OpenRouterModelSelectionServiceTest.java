package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterModelSelectionServiceTest {

    private OpenRouterProperties properties;
    private OpenRouterModelSelectionService service;

    @BeforeEach
    void setUp() {
        properties = new OpenRouterProperties();
        service = new OpenRouterModelSelectionService(properties);
    }

    @Test
    void resolveChain_ordersPrimaryThenFallback() {
        var chain = service.resolveChain(
                "AGENT_1",
                "meta-llama/llama-3.3-70b-instruct:free",
                "google/gemma-4-26b-a4b-it:free",
                Set.of(
                        "meta-llama/llama-3.3-70b-instruct:free",
                        "google/gemma-4-26b-a4b-it:free"
                )
        );

        assertThat(chain.modelsForRequest()).containsExactly(
                "meta-llama/llama-3.3-70b-instruct:free",
                "google/gemma-4-26b-a4b-it:free"
        );
        assertThat(chain.requestedModelId()).isEqualTo("meta-llama/llama-3.3-70b-instruct:free");
    }

    @Test
    void resolveChain_rejectsPaidFallback() {
        var chain = service.resolveChain(
                "AGENT_1",
                "meta-llama/llama-3.3-70b-instruct:free",
                "openai/gpt-4o",
                Set.of("meta-llama/llama-3.3-70b-instruct:free", "openai/gpt-4o")
        );

        assertThat(chain.modelsForRequest()).containsExactly("meta-llama/llama-3.3-70b-instruct:free");
    }

    @Test
    void resolveChain_appendsFreeRouterOnlyWhenEnabled() {
        properties.setUseFreeRouter(true);
        var chain = service.resolveChain(
                "AGENT_1",
                "meta-llama/llama-3.3-70b-instruct:free",
                "",
                Set.of("meta-llama/llama-3.3-70b-instruct:free", "openrouter/free")
        );

        assertThat(chain.modelsForRequest()).containsExactly(
                "meta-llama/llama-3.3-70b-instruct:free",
                "openrouter/free"
        );
    }
}
