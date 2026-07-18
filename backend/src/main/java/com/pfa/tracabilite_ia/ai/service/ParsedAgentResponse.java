package com.pfa.tracabilite_ia.ai.service;

import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;

public record ParsedAgentResponse(
        boolean valid,
        AIAnalysisResult result,
        String normalizedJson
) {
    public AIAnalysisResult requireValid() {
        if (!valid) {
            throw new IllegalStateException("Réponse agent invalide");
        }
        return result;
    }
}
