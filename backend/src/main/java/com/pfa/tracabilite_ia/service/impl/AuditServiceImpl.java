package com.pfa.tracabilite_ia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.*;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.mapper.ReponseAgentMapper;
import com.pfa.tracabilite_ia.mapper.ValidationMapper;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.ValidationActionRepository;
import com.pfa.tracabilite_ia.service.*;
import org.springframework.data.domain.PageRequest;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditServiceImpl implements AuditService {

    private final DecisionRepository decisionRepository;
    private final DecisionHistoryService decisionHistoryService;
    private final DecisionSourceService decisionSourceService;
    private final DecisionHashService decisionHashService;
    private final HashChainService hashChainService;
    private final ValidationActionRepository validationActionRepository;
    private final ValidationMapper validationMapper;
    private final ReponseAgentMapper reponseAgentMapper;
    private final ObjectMapper objectMapper;

    public AuditServiceImpl(DecisionRepository decisionRepository,
                              DecisionHistoryService decisionHistoryService,
                              DecisionSourceService decisionSourceService,
                              DecisionHashService decisionHashService,
                              HashChainService hashChainService,
                              ValidationActionRepository validationActionRepository,
                              ValidationMapper validationMapper,
                              ReponseAgentMapper reponseAgentMapper,
                              ObjectMapper objectMapper) {
        this.decisionRepository = decisionRepository;
        this.decisionHistoryService = decisionHistoryService;
        this.decisionSourceService = decisionSourceService;
        this.decisionHashService = decisionHashService;
        this.hashChainService = hashChainService;
        this.validationActionRepository = validationActionRepository;
        this.validationMapper = validationMapper;
        this.reponseAgentMapper = reponseAgentMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AuditDecisionResponse getDecisionAudit(UUID decisionId) {
        Decision decision = decisionRepository.findByIdWithFactors(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));

        Hibernate.initialize(decision.getReponsesAgents());

        return AuditDecisionResponse.builder()
                .decisionId(decision.getDecisionId())
                .prompt(decision.getPrompt())
                .contexte(decision.getContexte())
                .modelName(decision.getModelName())
                .modelVersion(decision.getModelVersion())
                .suggestedDecision(decision.getSuggestedDecision())
                .confidenceScore(decision.getConfidenceScore())
                .riskLevel(decision.getRiskLevel())
                .explanationSource(decision.getExplanationSource())
                .reponse(decision.getReponse())
                .consensus(readConsensus(decision.getConsensusJson()))
                .resumeConsensus(decision.getResumeConsensus())
                .statutValidation(decision.getStatutValidation())
                .humanDecision(decision.getHumanDecision())
                .validatorEmail(decision.getValidatorEmail())
                .businessDataHash(decision.getBusinessDataHash())
                .sourcesHash(decision.getSourcesHash())
                .agentResponsesHash(decision.getAgentResponsesHash())
                .previousHash(decision.getPreviousHash())
                .currentHash(decision.getCurrentHash())
                .integrityValid(decisionHashService.verifyDecisionIntegrity(decision))
                .timestamp(decision.getTimestamp())
                .agentResponses(reponseAgentMapper.toResponseList(decision.getReponsesAgents()))
                .validations(validationMapper.toResponseList(
                        validationActionRepository.findByDecisionDecisionIdOrderByTimestampDesc(decisionId)))
                .history(decisionHistoryService.listByDecision(decisionId))
                .sources(decisionSourceService.listByDecision(decisionId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditRecentResponse getRecentAudits(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<AuditRecentItemResponse> items = decisionRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(0, safeLimit))
                .stream()
                .map(decision -> AuditRecentItemResponse.builder()
                        .decisionId(decision.getDecisionId())
                        .prompt(decision.getPrompt())
                        .statutValidation(decision.getStatutValidation())
                        .integrityValid(decisionHashService.verifyDecisionIntegrity(decision))
                        .timestamp(decision.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return AuditRecentResponse.builder()
                .items(items)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditIntegritySummaryResponse getIntegritySummary() {
        List<Decision> decisions = decisionRepository.findAllByOrderByTimestampAsc();
        long valid = decisions.stream().filter(decisionHashService::verifyDecisionIntegrity).count();
        long total = decisions.size();

        return AuditIntegritySummaryResponse.builder()
                .totalDecisions(total)
                .validDecisions(valid)
                .invalidDecisions(total - valid)
                .chainIntact(hashChainService.verifierIntegrite())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private ConsensusResponse readConsensus(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ConsensusResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }
}
