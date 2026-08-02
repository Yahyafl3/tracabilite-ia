package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionIntegrityView {
    private String currentHash;
    private String previousHash;
    private String businessDataHash;
    private String sourcesHash;
    private String agentResponsesHash;
    private String explanation;
}
