package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.service.DecisionHashService;
import com.pfa.tracabilite_ia.service.HashChainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HashChainServiceImpl implements HashChainService {

    private final DecisionRepository decisionRepository;
    private final DecisionHashService decisionHashService;

    public HashChainServiceImpl(DecisionRepository decisionRepository,
                                DecisionHashService decisionHashService) {
        this.decisionRepository = decisionRepository;
        this.decisionHashService = decisionHashService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifierIntegrite() {
        List<Decision> decisions = decisionRepository.findAllByOrderByTimestampAsc();
        if (decisions.isEmpty()) {
            return true;
        }

        Decision previous = null;
        for (Decision decision : decisions) {
            if (decision.getCurrentHash() != null
                    && !decisionHashService.verifyDecisionIntegrity(decision)) {
                return false;
            }
            if (previous != null) {
                String expectedPrevious = previous.getCurrentHash();
                String actualPrevious = decision.getPreviousHash();
                if (expectedPrevious != null && actualPrevious != null
                        && !expectedPrevious.equals(actualPrevious)) {
                    return false;
                }
            }
            previous = decision;
        }
        return true;
    }
}
