package com.pfa.tracabilite_ia.ai.provider;

import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;

public interface AIProvider {

    String getProviderName();

    boolean isAvailable();

    AIAnalysisResult analyzeDecision(String prompt, String contexte);

    String summarizeContext(String contexte);

    String evaluateRisk(String prompt, String contexte);

    String generateRecommendation(String prompt, String contexte);

    String generateExplanation(String prompt, String contexte);
}
