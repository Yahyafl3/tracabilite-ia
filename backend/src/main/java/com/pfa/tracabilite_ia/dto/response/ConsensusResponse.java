package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsensusResponse {
    private String decisionConsensus;
    private Double confianceMoyenne;
    private int agentsConsultes;
    private int agentsReussis;
    private int successfulAgentCount;
    private Double agreementRate;
    private boolean consensusAvailable;
    private Map<String, Integer> votes;
    private String resume;
    private String note;
}
