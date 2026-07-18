package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterKeyStatusResponse {
    private boolean freeTier;
    private double dailyUsage;
    private Double remainingLimit;
    private boolean available;
    private String message;
}
