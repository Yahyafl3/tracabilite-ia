package com.pfa.tracabilite_ia.openrouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.exception.UnauthorizedActionException;
import com.pfa.tracabilite_ia.groq.GroqAgentRegistryService;
import com.pfa.tracabilite_ia.groq.GroqMultiAgentService;
import com.pfa.tracabilite_ia.mapper.DecisionMapper;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import com.pfa.tracabilite_ia.service.DecisionHashService;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import com.pfa.tracabilite_ia.service.DecisionSourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OpenRouterAgentRetryService {

    private static final String RETRY_HISTORY_NOTE_GROQ = "Relance agent Groq";
    private static final String RETRY_HISTORY_NOTE_OR = "Relance agent OpenRouter";

    private final DecisionRepository decisionRepository;
    private final ReponseAgentIARepository reponseAgentIARepository;
    private final OpenRouterMultiAgentService openRouterMultiAgentService;
    private final GroqMultiAgentService groqMultiAgentService;
    private final OpenRouterModelsCacheService modelsCacheService;
    private final OpenRouterPropertiesBridge propertiesBridge;
    private final OpenRouterConsensusService consensusService;
    private final OpenRouterKeyStatusService keyStatusService;
    private final DecisionHashService decisionHashService;
    private final DecisionSourceService decisionSourceService;
    private final DecisionHistoryService decisionHistoryService;
    private final DecisionMapper decisionMapper;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<UUID, Boolean> retryLocks = new ConcurrentHashMap<>();

    public OpenRouterAgentRetryService(DecisionRepository decisionRepository,
                                       ReponseAgentIARepository reponseAgentIARepository,
                                       OpenRouterMultiAgentService openRouterMultiAgentService,
                                       GroqMultiAgentService groqMultiAgentService,
                                       OpenRouterModelsCacheService modelsCacheService,
                                       OpenRouterPropertiesBridge propertiesBridge,
                                       OpenRouterConsensusService consensusService,
                                       OpenRouterKeyStatusService keyStatusService,
                                       DecisionHashService decisionHashService,
                                       DecisionSourceService decisionSourceService,
                                       DecisionHistoryService decisionHistoryService,
                                       DecisionMapper decisionMapper,
                                       ObjectMapper objectMapper) {
        this.decisionRepository = decisionRepository;
        this.reponseAgentIARepository = reponseAgentIARepository;
        this.openRouterMultiAgentService = openRouterMultiAgentService;
        this.groqMultiAgentService = groqMultiAgentService;
        this.modelsCacheService = modelsCacheService;
        this.propertiesBridge = propertiesBridge;
        this.consensusService = consensusService;
        this.keyStatusService = keyStatusService;
        this.decisionHashService = decisionHashService;
        this.decisionSourceService = decisionSourceService;
        this.decisionHistoryService = decisionHistoryService;
        this.decisionMapper = decisionMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DecisionResponse retryFailedAgents(UUID decisionId, Utilisateur user) {
        if (retryLocks.putIfAbsent(decisionId, Boolean.TRUE) != null) {
            throw new UnauthorizedActionException("Une relance agents est deja en cours pour cette decision.");
        }

        try {
            Decision decision = decisionRepository.findById(decisionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable"));

            List<ReponseAgentIA> existing = reponseAgentIARepository
                    .findByDecisionDecisionIdOrderByAgentKeyAsc(decisionId);

            boolean useGroq = existing.stream()
                    .anyMatch(a -> GroqAgentRegistryService.PROVIDER.equalsIgnoreCase(a.getProvider()));

            List<ReponseAgentIA> retryable = existing.stream()
                    .filter(agent -> useGroq
                            ? GroqMultiAgentService.isRetryableFailure(agent)
                            : OpenRouterMultiAgentService.isRetryableFailure(agent))
                    .toList();

            if (retryable.isEmpty()) {
                throw new UnauthorizedActionException("Aucun agent eligible a la relance.");
            }

            if (!useGroq && !keyStatusService.hasQuotaForAgents(retryable.size())) {
                throw new UnauthorizedActionException(
                        "Quota OpenRouter insuffisant. L'analyse ML reste disponible.");
            }

            if (!useGroq) {
                modelsCacheService.refresh();
            }

            StatutDecisionEnum status = decision.getStatutValidation();
            String note = useGroq ? RETRY_HISTORY_NOTE_GROQ : RETRY_HISTORY_NOTE_OR;

            decisionHistoryService.record(decision, DecisionHistoryAction.OPENROUTER_ANALYSIS_STARTED,
                    status, status, user.getId(), user.getEmail(), null, note,
                    Map.of("agents", retryable.stream().map(ReponseAgentIA::getAgentKey).toList(),
                            "retry", true, "provider", useGroq ? "GROQ" : "OPENROUTER"));

            for (int index = 0; index < retryable.size(); index++) {
                if (index > 0) {
                    pauseBetweenAgents();
                }
                ReponseAgentIA current = retryable.get(index);
                Map<String, Object> previousAttempt = snapshotAgent(current);
                decisionHistoryService.record(decision, DecisionHistoryAction.AGENT_RESPONSE_FAILED,
                        status, status, user.getId(), user.getEmail(), null,
                        note + " " + current.getAgentKey(),
                        Map.of("previousAttempt", previousAttempt));
                if (useGroq) {
                    groqMultiAgentService.retryAgentResponse(decision, current, user);
                } else {
                    openRouterMultiAgentService.retryAgentResponse(decision, current, user);
                }
            }

            List<ReponseAgentIA> allResponses = reponseAgentIARepository
                    .findByDecisionDecisionIdOrderByAgentKeyAsc(decisionId);
            decision.getReponsesAgents().clear();
            decision.getReponsesAgents().addAll(allResponses);

            var consensus = consensusService.compute(allResponses);
            decision.setConsensusJson(writeJson(consensus));
            decision.setResumeConsensus(consensus.getResume());

            decisionHashService.refreshHashComponents(decision, allResponses);
            decisionSourceService.refreshSourcesHash(decision);
            chainWithPrevious(decision);

            Decision saved = decisionRepository.save(decision);
            decisionHistoryService.record(decision, DecisionHistoryAction.CONSENSUS_CALCULATED,
                    status, status, user.getId(), user.getEmail(), null, null,
                    Map.of("decisionConsensus", consensus.getDecisionConsensus(), "afterRetry", true));

            return decisionMapper.toResponse(saved);
        } finally {
            retryLocks.remove(decisionId);
        }
    }

    private Map<String, Object> snapshotAgent(ReponseAgentIA agent) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("statut", agent.getStatut().name());
        snapshot.put("codeErreur", agent.getCodeErreur());
        snapshot.put("fallbackReason", agent.getFallbackReason());
        snapshot.put("requestedModelId", agent.getRequestedModelId());
        snapshot.put("actualModelId", agent.getActualModelId());
        snapshot.put("responseHash", agent.getResponseHash());
        snapshot.put("retryCount", agent.getRetryCount());
        return snapshot;
    }

    private void pauseBetweenAgents() {
        int delayMs = propertiesBridge.agentDelayMs();
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void chainWithPrevious(Decision decision) {
        decisionRepository.findTopByDecisionIdNotOrderByTimestampDesc(decision.getDecisionId())
                .ifPresentOrElse(
                        decision::chainerAvecPrecedent,
                        () -> {
                            decision.setPreviousHash(null);
                            decision.setCurrentHash(decision.calculerHash());
                        }
                );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @org.springframework.stereotype.Component
    public static class OpenRouterPropertiesBridge {
        private final com.pfa.tracabilite_ia.config.OpenRouterProperties openRouterProperties;
        private final com.pfa.tracabilite_ia.config.GroqProperties groqProperties;

        public OpenRouterPropertiesBridge(com.pfa.tracabilite_ia.config.OpenRouterProperties openRouterProperties,
                                          com.pfa.tracabilite_ia.config.GroqProperties groqProperties) {
            this.openRouterProperties = openRouterProperties;
            this.groqProperties = groqProperties;
        }

        public int agentDelayMs() {
            if (groqProperties.isConfigured()) {
                return groqProperties.getAgentDelayMs();
            }
            return openRouterProperties.getAgentDelayMs();
        }
    }
}
