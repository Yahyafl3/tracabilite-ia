package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.DecisionAnalysisRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionAnalysisResponse;

public interface AIService {

    DecisionAnalysisResponse analyzeDecision(DecisionAnalysisRequest request);

    String summarizeContext(String contexte);

    String evaluateRisk(DecisionAnalysisRequest request);

    String generateRecommendation(DecisionAnalysisRequest request);

    String generateExplanation(DecisionAnalysisRequest request);
}
