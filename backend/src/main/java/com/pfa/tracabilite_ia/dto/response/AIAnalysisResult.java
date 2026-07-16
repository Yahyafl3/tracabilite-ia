package com.pfa.tracabilite_ia.dto.response;

import java.util.List;

public class AIAnalysisResult {

    private String suggestedDecision;
    private double confidence;
    private String riskLevel;
    private String summary;
    private String explanation;
    private List<String> recommendations;

    public String getSuggestedDecision() {
        return suggestedDecision;
    }

    public void setSuggestedDecision(String suggestedDecision) {
        this.suggestedDecision = suggestedDecision;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
