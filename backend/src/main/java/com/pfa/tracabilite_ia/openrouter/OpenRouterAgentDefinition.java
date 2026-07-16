package com.pfa.tracabilite_ia.openrouter;

public record OpenRouterAgentDefinition(
        String agentKey,
        String displayName,
        String modelId,
        String provider,
        int orderIndex,
        boolean active
) {
}
