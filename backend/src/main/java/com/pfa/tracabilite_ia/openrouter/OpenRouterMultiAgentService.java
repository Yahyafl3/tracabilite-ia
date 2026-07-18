package com.pfa.tracabilite_ia.openrouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.ai.client.OpenRouterClient;
import com.pfa.tracabilite_ia.ai.dto.OpenRouterChatResult;
import com.pfa.tracabilite_ia.ai.service.OpenRouterResponseParser;
import com.pfa.tracabilite_ia.ai.service.ParsedAgentResponse;
import com.pfa.tracabilite_ia.ai.service.PromptTemplateService;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import com.pfa.tracabilite_ia.dto.response.ConsensusResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OpenRouterMultiAgentService {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterMultiAgentService.class);
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String QUOTA_MESSAGE = "Quota OpenRouter insuffisant. L'analyse ML reste disponible.";

    private final OpenRouterClient openRouterClient;
    private final OpenRouterAgentRegistryService agentRegistryService;
    private final OpenRouterConsensusService consensusService;
    private final OpenRouterResponseParser responseParser;
    private final PromptTemplateService promptTemplateService;
    private final ReponseAgentIARepository reponseAgentIARepository;
    private final ObjectMapper objectMapper;
    private final OpenRouterProperties openRouterProperties;
    private final DecisionHistoryService decisionHistoryService;
    private final OpenRouterModelsCacheService modelsCacheService;
    private final OpenRouterModelSelectionService modelSelectionService;
    private final OpenRouterKeyStatusService keyStatusService;

    public OpenRouterMultiAgentService(OpenRouterClient openRouterClient,
                                       OpenRouterAgentRegistryService agentRegistryService,
                                       OpenRouterConsensusService consensusService,
                                       OpenRouterResponseParser responseParser,
                                       PromptTemplateService promptTemplateService,
                                       ReponseAgentIARepository reponseAgentIARepository,
                                       ObjectMapper objectMapper,
                                       OpenRouterProperties openRouterProperties,
                                       DecisionHistoryService decisionHistoryService,
                                       OpenRouterModelsCacheService modelsCacheService,
                                       OpenRouterModelSelectionService modelSelectionService,
                                       OpenRouterKeyStatusService keyStatusService) {
        this.openRouterClient = openRouterClient;
        this.agentRegistryService = agentRegistryService;
        this.consensusService = consensusService;
        this.responseParser = responseParser;
        this.promptTemplateService = promptTemplateService;
        this.reponseAgentIARepository = reponseAgentIARepository;
        this.objectMapper = objectMapper;
        this.openRouterProperties = openRouterProperties;
        this.decisionHistoryService = decisionHistoryService;
        this.modelsCacheService = modelsCacheService;
        this.modelSelectionService = modelSelectionService;
        this.keyStatusService = keyStatusService;
    }

    @Transactional
    public OpenRouterAnalysisBundle analyzeDecisionAgents(Decision decision, String prompt, String contexte) {
        return analyzeDecisionAgents(decision, prompt, contexte, null);
    }

    @Transactional
    public OpenRouterAnalysisBundle analyzeDecisionAgents(Decision decision, String prompt, String contexte,
                                                            Utilisateur user) {
        decision.getReponsesAgents().clear();

        StatutDecisionEnum status = decision.getStatutValidation();
        recordHistory(decision, DecisionHistoryAction.OPENROUTER_ANALYSIS_STARTED, status, status, user, null);

        List<OpenRouterAgentDefinition> agents = agentRegistryService.configuredAgents();
        if (!openRouterProperties.isConfigured()) {
            return skipAgents(decision, status, user, "OPENROUTER_API_KEY non configuree");
        }

        if (!keyStatusService.hasQuotaForAgents(agents.size())) {
            return skipAgents(decision, status, user, QUOTA_MESSAGE);
        }

        String systemPrompt = promptTemplateService.systemPromptForDecisionAnalysis();
        String userPrompt = promptTemplateService.userPromptForDecisionAnalysis(prompt, contexte);
        String correlationId = currentCorrelationId();
        Set<String> availableModelIds = modelsCacheService.availableModelIds();

        List<ReponseAgentIA> responses = new ArrayList<>();
        for (int index = 0; index < agents.size(); index++) {
            if (index > 0) {
                pauseBetweenAgents();
            }
            OpenRouterAgentDefinition agent = agents.get(index);
            ReponseAgentIA entity = invokeAgent(decision, agent, systemPrompt, userPrompt, correlationId, availableModelIds);
            responses.add(entity);
            decision.getReponsesAgents().add(entity);
            recordAgentHistory(decision, entity, user);
        }

        reponseAgentIARepository.saveAll(responses);
        return finalizeConsensus(decision, responses, status, user);
    }

    private OpenRouterAnalysisBundle skipAgents(Decision decision, StatutDecisionEnum status,
                                                Utilisateur user, String message) {
        log.warn("OpenRouter ignore pour decision {}: {}", decision.getDecisionId(), message);
        ConsensusResponse consensus = consensusService.buildSkippedConsensus(message);
        decision.setConsensusJson(writeJson(consensus));
        decision.setResumeConsensus(consensus.getResume());
        recordHistory(decision, DecisionHistoryAction.CONSENSUS_CALCULATED, status, status, user,
                Map.of("skipped", true, "message", message));
        return new OpenRouterAnalysisBundle(List.of(), consensus);
    }

    private OpenRouterAnalysisBundle finalizeConsensus(Decision decision, List<ReponseAgentIA> responses,
                                                       StatutDecisionEnum status, Utilisateur user) {
        ConsensusResponse consensus = consensusService.compute(responses);
        decision.setConsensusJson(writeJson(consensus));
        decision.setResumeConsensus(consensus.getResume());

        Map<String, Object> consensusEvent = new HashMap<>();
        consensusEvent.put("decisionConsensus", consensus.getDecisionConsensus());
        consensusEvent.put("agentsReussis", consensus.getAgentsReussis());
        consensusEvent.put("agentsConsultes", consensus.getAgentsConsultes());
        recordHistory(decision, DecisionHistoryAction.CONSENSUS_CALCULATED, status, status, user, consensusEvent);

        return new OpenRouterAnalysisBundle(responses, consensus);
    }

    private void recordAgentHistory(Decision decision, ReponseAgentIA entity, Utilisateur user) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("agentKey", entity.getAgentKey());
        eventData.put("requestedModelId", entity.getRequestedModelId());
        eventData.put("actualModelId", entity.getActualModelId());
        eventData.put("fallbackUsed", entity.getFallbackUsed());
        eventData.put("fallbackReason", entity.getFallbackReason());
        eventData.put("retryCount", entity.getRetryCount());
        eventData.put("statut", entity.getStatut().name());
        if (entity.getDecisionProposee() != null) {
            eventData.put("decisionProposee", entity.getDecisionProposee());
        }
        if (entity.getCodeErreur() != null) {
            eventData.put("codeErreur", entity.getCodeErreur());
        }

        DecisionHistoryAction action = entity.getStatut() == StatutReponseAgentEnum.SUCCESS
                ? DecisionHistoryAction.AGENT_RESPONSE_SUCCESS
                : DecisionHistoryAction.AGENT_RESPONSE_FAILED;
        recordHistory(decision, action, decision.getStatutValidation(), decision.getStatutValidation(), user, eventData);
    }

    private void recordHistory(Decision decision, DecisionHistoryAction action,
                               StatutDecisionEnum previous, StatutDecisionEnum next,
                               Utilisateur user, Map<String, Object> eventData) {
        decisionHistoryService.record(decision, action, previous, next,
                user != null ? user.getId() : null,
                user != null ? user.getEmail() : null,
                null, null, eventData);
    }

    private ReponseAgentIA invokeAgent(Decision decision,
                                       OpenRouterAgentDefinition agent,
                                       String systemPrompt,
                                       String userPrompt,
                                       String correlationId,
                                       Set<String> availableModelIds) {
        return invokeAgent(decision, agent, systemPrompt, userPrompt, correlationId, availableModelIds, null);
    }

    public ReponseAgentIA retryAgentResponse(Decision decision,
                                             ReponseAgentIA existing,
                                             Utilisateur user) {
        OpenRouterAgentDefinition agent = agentRegistryService.findAgent(existing.getAgentKey());
        String systemPrompt = promptTemplateService.systemPromptForDecisionAnalysis();
        String userPrompt = promptTemplateService.userPromptForDecisionAnalysis(
                decision.getPrompt() != null ? decision.getPrompt() : "",
                decision.getContexte() != null ? decision.getContexte() : ""
        );
        Set<String> availableModelIds = modelsCacheService.availableModelIds();
        ReponseAgentIA refreshed = invokeAgent(
                decision, agent, systemPrompt, userPrompt, currentCorrelationId(), availableModelIds, existing);
        reponseAgentIARepository.save(refreshed);
        recordAgentHistory(decision, refreshed, user);
        return refreshed;
    }

    public static boolean isRetryableFailure(ReponseAgentIA agent) {
        if (agent.getStatut() == StatutReponseAgentEnum.SUCCESS) {
            return false;
        }
        String reason = agent.getFallbackReason();
        if (reason != null) {
            return Set.of("RATE_LIMITED", "TIMEOUT", "TEMPORARILY_UNAVAILABLE").contains(reason);
        }
        if (agent.getStatut() == StatutReponseAgentEnum.TIMEOUT) {
            return true;
        }
        if (agent.getCodeErreur() != null) {
            try {
                OpenRouterErrorCode code = OpenRouterErrorCode.valueOf(agent.getCodeErreur());
                return code == OpenRouterErrorCode.OPENROUTER_RATE_LIMITED
                        || code == OpenRouterErrorCode.OPENROUTER_TIMEOUT
                        || code == OpenRouterErrorCode.OPENROUTER_UNAVAILABLE;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
        return false;
    }

    private ReponseAgentIA invokeAgent(Decision decision,
                                       OpenRouterAgentDefinition agent,
                                       String systemPrompt,
                                       String userPrompt,
                                       String correlationId,
                                       Set<String> availableModelIds,
                                       ReponseAgentIA existingEntity) {
        ReponseAgentIA entity = existingEntity != null ? existingEntity : new ReponseAgentIA();
        if (existingEntity == null) {
            entity.setDecision(decision);
        } else {
            clearAgentResponseFields(entity);
        }
        entity.setAgentKey(agent.agentKey());
        entity.setProvider(agent.provider());
        entity.setRequestedModelId(agent.modelId());
        entity.setModelId(agent.modelId());
        entity.setModelName(agent.displayName());
        entity.setFallbackUsed(false);

        String fallbackModelId = openRouterProperties.fallbackForAgentKey(agent.agentKey());
        OpenRouterModelSelectionService.ModelChain chain = modelSelectionService.resolveChain(
                agent.agentKey(), agent.modelId(), fallbackModelId, availableModelIds);

        if (!chain.callable()) {
            entity.setStatut(StatutReponseAgentEnum.MODEL_UNAVAILABLE);
            entity.setCodeErreur(OpenRouterErrorCode.MODEL_UNAVAILABLE.name());
            entity.setActualModelId(null);
            log.warn("Agent {} sans modele callable (primary={}, fallback={})",
                    agent.agentKey(), agent.modelId(), fallbackModelId);
            return entity;
        }

        try {
            OpenRouterChatResult chatResult = openRouterClient.chatCompletionWithModels(
                    chain.modelsForRequest(),
                    chain.requestedModelId(),
                    systemPrompt,
                    userPrompt,
                    correlationId
            );
            applyChatSuccess(entity, chatResult);
            return entity;
        } catch (OpenRouterException ex) {
            return markFailure(entity, ex);
        } catch (ResourceAccessException ex) {
            entity.setStatut(StatutReponseAgentEnum.TIMEOUT);
            entity.setCodeErreur(OpenRouterErrorCode.OPENROUTER_TIMEOUT.name());
            entity.setFallbackReason("TIMEOUT");
            entity.setExplication(ex.getMessage());
            return entity;
        } catch (Exception ex) {
            entity.setStatut(StatutReponseAgentEnum.FAILURE);
            entity.setCodeErreur(OpenRouterErrorCode.OPENROUTER_UNAVAILABLE.name());
            entity.setFallbackReason("TEMPORARILY_UNAVAILABLE");
            entity.setExplication(ex.getMessage());
            log.warn("Echec agent {}: {}", agent.agentKey(), ex.getMessage());
            return entity;
        }
    }

    private void applyChatSuccess(ReponseAgentIA entity, OpenRouterChatResult chatResult) {
        String rawContent = chatResult.getRawContent();
        ParsedAgentResponse parsed = responseParser.parse(rawContent);
        AIAnalysisResult result = parsed.result();

        entity.setReponseBrute(rawContent);
        entity.setReponseNormalisee(parsed.normalizedJson());
        entity.setDecisionProposee(result.getSuggestedDecision());
        entity.setConfianceDeclaree(result.getConfidence());
        entity.setNiveauRisque(result.getRiskLevel());
        entity.setResume(result.getSummary());
        entity.setExplication(result.getExplanation());
        entity.setRecommandationsJson(writeJson(result.getRecommendations()));
        entity.setDureeMs(chatResult.getDurationMs());
        entity.setNombreTokens(chatResult.getTotalTokens());
        entity.setRetryCount(chatResult.getRetryCount());
        entity.setRequestedModelId(chatResult.getRequestedModelId());
        entity.setActualModelId(chatResult.getActualModelId());
        entity.setFallbackUsed(chatResult.isFallbackUsed());
        entity.setFallbackReason(chatResult.getFallbackReason());
        entity.setResponseHash(chatResult.getResponseHash());

        if (Boolean.TRUE.equals(chatResult.isFallbackUsed()) && chatResult.getActualModelId() != null) {
            entity.setModelId(chatResult.getActualModelId());
            entity.setModelName(OpenRouterAgentDisplayNames.resolve(chatResult.getActualModelId()));
        }

        if (parsed.valid()) {
            entity.setStatut(StatutReponseAgentEnum.SUCCESS);
        } else {
            entity.setStatut(StatutReponseAgentEnum.INVALID_RESPONSE);
            entity.setCodeErreur(OpenRouterErrorCode.OPENROUTER_INVALID_RESPONSE.name());
        }
    }

    private ReponseAgentIA markFailure(ReponseAgentIA entity, OpenRouterException ex) {
        entity.setCodeErreur(ex.getErrorCode().name());
        entity.setExplication(ex.getMessage());
        entity.setFallbackReason(mapFallbackReason(ex.getErrorCode()));
        entity.setStatut(switch (ex.getErrorCode()) {
            case OPENROUTER_TIMEOUT -> StatutReponseAgentEnum.TIMEOUT;
            case MODEL_UNAVAILABLE, OPENROUTER_MODEL_NOT_FOUND -> StatutReponseAgentEnum.MODEL_UNAVAILABLE;
            default -> StatutReponseAgentEnum.FAILURE;
        });
        return entity;
    }

    private String mapFallbackReason(OpenRouterErrorCode code) {
        return switch (code) {
            case OPENROUTER_RATE_LIMITED -> "RATE_LIMITED";
            case OPENROUTER_TIMEOUT -> "TIMEOUT";
            case OPENROUTER_UNAVAILABLE -> "TEMPORARILY_UNAVAILABLE";
            case OPENROUTER_MODEL_NOT_FOUND, MODEL_UNAVAILABLE -> "MODEL_UNAVAILABLE";
            default -> code.name();
        };
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Erreur de serialisation JSON", ex);
        }
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        return correlationId != null ? correlationId : "unknown";
    }

    private void pauseBetweenAgents() {
        int delayMs = openRouterProperties.getAgentDelayMs();
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Pause inter-agents interrompue");
        }
    }

    private void clearAgentResponseFields(ReponseAgentIA entity) {
        entity.setReponseBrute(null);
        entity.setReponseNormalisee(null);
        entity.setDecisionProposee(null);
        entity.setConfianceDeclaree(null);
        entity.setNiveauRisque(null);
        entity.setResume(null);
        entity.setExplication(null);
        entity.setRecommandationsJson(null);
        entity.setDureeMs(null);
        entity.setNombreTokens(null);
        entity.setCodeErreur(null);
        entity.setActualModelId(null);
        entity.setFallbackUsed(false);
        entity.setFallbackReason(null);
        entity.setResponseHash(null);
        entity.setRetryCount(null);
    }

    public record OpenRouterAnalysisBundle(
            List<ReponseAgentIA> responses,
            ConsensusResponse consensus
    ) {
    }
}
