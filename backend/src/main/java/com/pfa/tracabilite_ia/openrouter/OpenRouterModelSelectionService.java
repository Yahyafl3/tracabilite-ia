package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class OpenRouterModelSelectionService {

    public static final String FREE_ROUTER_MODEL = "openrouter/free";

    private final OpenRouterProperties properties;

    public OpenRouterModelSelectionService(OpenRouterProperties properties) {
        this.properties = properties;
    }

    public ModelChain resolveChain(String agentKey, String primaryModelId, String fallbackModelId,
                                   Set<String> availableModelIds) {
        List<String> ordered = new ArrayList<>();

        if (isFreeModel(primaryModelId)) {
            ordered.add(primaryModelId);
        }

        if (isEligibleFallback(fallbackModelId, availableModelIds)) {
            if (!ordered.contains(fallbackModelId)) {
                ordered.add(fallbackModelId);
            }
        }

        if (properties.isUseFreeRouter() && isEligibleFallback(FREE_ROUTER_MODEL, availableModelIds)) {
            if (!ordered.contains(FREE_ROUTER_MODEL)) {
                ordered.add(FREE_ROUTER_MODEL);
            }
        }

        return new ModelChain(agentKey, primaryModelId, fallbackModelId, List.copyOf(ordered), !ordered.isEmpty());
    }

    public boolean isFreeModel(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return false;
        }
        if (FREE_ROUTER_MODEL.equals(modelId)) {
            return true;
        }
        return modelId.contains(":free");
    }

    private boolean isEligibleFallback(String modelId, Set<String> availableModelIds) {
        if (modelId == null || modelId.isBlank()) {
            return false;
        }
        if (!isFreeModel(modelId)) {
            return false;
        }
        if (availableModelIds == null || availableModelIds.isEmpty()) {
            return true;
        }
        return availableModelIds.contains(modelId);
    }

    public record ModelChain(
            String agentKey,
            String requestedModelId,
            String configuredFallbackModelId,
            List<String> modelsForRequest,
            boolean callable
    ) {
    }
}
