package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionResponse {
    private String decision;
    private String domain;
    private Double scoreConfiance;
    private String riskLevel;
    private Map<String, Double> probabilities;
    private List<DecisionFactor> factors;
    private String explanationSource;
    private Map<String, Object> features;
    private Map<String, Object> model;
    private String timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DecisionFactor {
        private String name;
        private Object value;
        private Double shapValue;
        private String impact;
        private Integer rank;
        private Double contributionPercent;
    }
}
