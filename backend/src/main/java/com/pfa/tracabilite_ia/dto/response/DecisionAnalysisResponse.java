package com.pfa.tracabilite_ia.dto.response;

import java.util.UUID;

public class DecisionAnalysisResponse {

    private AIAnalysisResult analysis;
    private UUID traceId;
    private String correlationId;
    private String provider;
    private String model;

    public AIAnalysisResult getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AIAnalysisResult analysis) {
        this.analysis = analysis;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
