package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterModelStatusResponse {
    private String agentKey;
    private String displayName;
    private String modelId;
    private String provider;
    private boolean configured;
    private boolean available;
    private String status;
}
