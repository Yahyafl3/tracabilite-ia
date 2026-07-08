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
    private String contenu;
    private String contexte;
    private Double scoreConfiance;
    private String raison;
    private Map<String, Double> probabilities;
    private List<DecisionFactor> factors;
    private String timestamp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DecisionFactor {
        private String name;
        private Double importance;
    }
}
