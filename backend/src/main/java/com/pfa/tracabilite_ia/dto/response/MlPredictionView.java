package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlPredictionView {
    private String decision;
    private Double confidenceScore;
    private String riskLevel;
    private String modelName;
    private String modelVersion;
}
