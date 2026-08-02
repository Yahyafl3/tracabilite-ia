package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainPredictionResponse {
    private String analysisId;
    private String domain;
    private String prediction;
    private String riskLevel;
    private Double probability;
    private Double confidence;
    private String recommendation;
    private String modelVersion;
    private String datasetVersion;
    private String modelType;
    private String explanationMethod;
    private List<Factor> factors;
    private String disclaimer;
    private String generatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Factor {
        private String feature;
        private String impact;
        private Double importance;
    }
}
