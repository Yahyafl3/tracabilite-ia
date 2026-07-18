package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Charge une décision avec ses données strictement scoped par decisionId.
 */
@Service
public class DecisionScopeService {

    private final DecisionRepository decisionRepository;
    private final ReponseAgentIARepository reponseAgentIARepository;

    public DecisionScopeService(DecisionRepository decisionRepository,
                                ReponseAgentIARepository reponseAgentIARepository) {
        this.decisionRepository = decisionRepository;
        this.reponseAgentIARepository = reponseAgentIARepository;
    }

    @Transactional(readOnly = true)
    public Decision loadForRead(UUID decisionId) {
        Decision decision = decisionRepository.findByIdWithFactors(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));
        attachScopedAgentResponses(decision);
        return decision;
    }

    @Transactional(readOnly = true)
    public Decision loadForValidation(UUID decisionId) {
        return loadForRead(decisionId);
    }

    public void attachScopedAgentResponses(Decision decision) {
        List<ReponseAgentIA> scopedAgents = reponseAgentIARepository
                .findByDecisionDecisionIdOrderByAgentKeyAsc(decision.getDecisionId());
        decision.getReponsesAgents().clear();
        for (ReponseAgentIA agent : scopedAgents) {
            agent.setDecision(decision);
            decision.getReponsesAgents().add(agent);
        }
    }
}
