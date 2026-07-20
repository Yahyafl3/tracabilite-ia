package com.pfa.tracabilite_ia.groq;

public record GroqAgentDefinition(
        String agentKey,
        String displayName,
        String modelId,
        String provider,
        int orderIndex,
        boolean active
) {
}
