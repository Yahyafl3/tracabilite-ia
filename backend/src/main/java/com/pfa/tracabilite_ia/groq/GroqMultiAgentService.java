package com.pfa.tracabilite_ia.groq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.ai.client.GroqClient;
import com.pfa.tracabilite_ia.ai.dto.GroqChatResult;
import com.pfa.tracabilite_ia.ai.service.OpenRouterResponseParser;
import com.pfa.tracabilite_ia.ai.service.ParsedAgentResponse;
import com.pfa.tracabilite_ia.ai.service.PromptTemplateService;
import com.pfa.tracabilite_ia.config.GroqProperties;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import com.pfa.tracabilite_ia.dto.response.ConsensusResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.exception.GroqErrorCode;
import com.pfa.tracabilite_ia.exception.GroqException;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import com.pfa.tracabilite_ia.openrouter.OpenRouterConsensusService;
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
public class GroqMultiAgentService {

    private static final Logger log = LoggerFactory.getLogger(GroqMultiAgentService.class);
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String SKIP_MESSAGE = "GROQ_API_KEY non configuree. L'analyse ML reste disponible.";

    private final GroqClient groqClient;
    private final GroqAgentRegistryService agentRegistryService;
    private final OpenRouterConsensusService consensusService;
    private final OpenRouterResponseParser responseParser;
    private final PromptTemplateService promptTemplateService;
    private final ReponseAgentIARepository reponseAgentIARepository;
    private final ObjectMapper objectMapper;
    private final GroqProperties groqProperties;
    private final DecisionHistoryService decisionHistoryService;

    public GroqMultiAgentService(GroqClient groqClient,
                                 GroqAgentRegistryService agentRegistryService,
                                 OpenRouterConsensusService consensusService,
                                 OpenRouterResponseParser responseParser,
                                 PromptTemplateService promptTemplateService,
                                 ReponseAgentIARepository reponseAgentIARepository,
                                 ObjectMapper objectMapper,
                                 GroqProperties groqProperties,
                                 DecisionHistoryService decisionHistoryService) {
        this.groqClient = groqClient;
        this.agentRegistryService = agentRegistryService;
        this.consensusService = consensusService;
        this.responseParser = responseParser;
        this.promptTemplateService = promptTemplateService;
        this.reponseAgentIARepository = reponseAgentIARepository;
        this.objectMapper = objectMapper;
        this.groqProperties = groqProperties;
        this.decisionHistoryService = decisionHistoryService;
    }

    @Transactional
    public GroqAnalysisBundle analyzeDecisionAgents(Decision decision, String prompt, String contexte) {
        return analyzeDecisionAgents(decision, prompt, contexte, null);
    }

    @Transactional
    public GroqAnalysisBundle analyzeDecisionAgents(Decision decision, String prompt, String contexte,
                                                    Utilisateur user) {
        decision.getReponsesAgents().clear();

        StatutDecisionEnum status = decision.getStatutValidation();
        recordHistory(decision, DecisionHistoryAction.OPENROUTER_ANALYSIS_STARTED, status, status, user, null);

        List<GroqAgentDefinition> agents = agentRegistryService.configuredAgents();
        if (!groqProperties.isConfigured()) {
            return skipAgents(decision, status, user, SKIP_MESSAGE);
        }

        String systemPrompt = promptTemplateService.systemPromptForDecisionAnalysis();
        String userPrompt = promptTemplateService.userPromptForDecisionAnalysis(prompt, contexte);
        String correlationId = currentCorrelationId();

        List<ReponseAgentIA> responses = new ArrayList<>();
        for (int index = 0; index < agents.size(); index++) {
            if (index > 0) {
                pauseBetweenAgents();
            }
            GroqAgentDefinition agent = agents.get(index);
            ReponseAgentIA entity = invokeAgent(decision, agent, systemPrompt, userPrompt, correlationId, null);
            responses.add(entity);
            decision.getReponsesAgents().add(entity);
            recordAgentHistory(decision, entity, user);
        }

        reponseAgentIARepository.saveAll(responses);
        return finalizeConsensus(decision, responses, status, user);
    }

    public ReponseAgentIA retryAgentResponse(Decision decision,
                                             ReponseAgentIA existing,
                                             Utilisateur user) {
        GroqAgentDefinition agent = agentRegistryService.findAgent(existing.getAgentKey());
        String systemPrompt = promptTemplateService.systemPromptForDecisionAnalysis();
        String userPrompt = promptTemplateService.userPromptForDecisionAnalysis(
                decision.getPrompt() != null ? decision.getPrompt() : "",
                decision.getContexte() != null ? decision.getContexte() : ""
        );
        ReponseAgentIA refreshed = invokeAgent(
                decision, agent, systemPrompt, userPrompt, currentCorrelationId(), existing);
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
                GroqErrorCode code = GroqErrorCode.valueOf(agent.getCodeErreur());
                return code == GroqErrorCode.GROQ_RATE_LIMITED
                        || code == GroqErrorCode.GROQ_TIMEOUT
                        || code == GroqErrorCode.GROQ_UNAVAILABLE;
            } catch (IllegalArgumentException ignored) {
                // historical OpenRouter codes
                return Set.of("OPENROUTER_RATE_LIMITED", "OPENROUTER_TIMEOUT", "OPENROUTER_UNAVAILABLE")
                        .contains(agent.getCodeErreur());
            }
        }
        return false;
    }

    private GroqAnalysisBundle skipAgents(Decision decision, StatutDecisionEnum status,
                                          Utilisateur user, String message) {
        log.warn("Groq ignore pour decision {}: {}", decision.getDecisionId(), message);
        ConsensusResponse consensus = consensusService.buildSkippedConsensus(message);
        decision.setConsensusJson(writeJson(consensus));
        decision.setResumeConsensus(consensus.getResume());
        recordHistory(decision, DecisionHistoryAction.CONSENSUS_CALCULATED, status, status, user,
                Map.of("skipped", true, "message", message, "provider", GroqAgentRegistryService.PROVIDER));
        return new GroqAnalysisBundle(List.of(), consensus);
    }

    private GroqAnalysisBundle finalizeConsensus(Decision decision, List<ReponseAgentIA> responses,
                                                 StatutDecisionEnum status, Utilisateur user) {
        ConsensusResponse consensus = consensusService.compute(responses);
        decision.setConsensusJson(writeJson(consensus));
        decision.setResumeConsensus(consensus.getResume());

        Map<String, Object> consensusEvent = new HashMap<>();
        consensusEvent.put("decisionConsensus", consensus.getDecisionConsensus());
        consensusEvent.put("agentsReussis", consensus.getAgentsReussis());
        consensusEvent.put("agentsConsultes", consensus.getAgentsConsultes());
        consensusEvent.put("provider", GroqAgentRegistryService.PROVIDER);
        recordHistory(decision, DecisionHistoryAction.CONSENSUS_CALCULATED, status, status, user, consensusEvent);

        return new GroqAnalysisBundle(responses, consensus);
    }

    private ReponseAgentIA invokeAgent(Decision decision,
                                       GroqAgentDefinition agent,
                                       String systemPrompt,
                                       String userPrompt,
                                       String correlationId,
                                       ReponseAgentIA existingEntity) {
        ReponseAgentIA entity = existingEntity != null ? existingEntity : new ReponseAgentIA();
        if (existingEntity == null) {
            entity.setDecision(decision);
        } else {
            clearAgentResponseFields(entity);
        }
        entity.setAgentKey(agent.agentKey());
        entity.setProvider(GroqAgentRegistryService.PROVIDER);
        entity.setRequestedModelId(agent.modelId());
        entity.setModelId(agent.modelId());
        entity.setModelName(agent.displayName());
        entity.setFallbackUsed(false);

        try {
            GroqChatResult chatResult = groqClient.chatCompletion(
                    agent.modelId(), systemPrompt, userPrompt, correlationId);
            applyChatSuccess(entity, chatResult);
            return entity;
        } catch (GroqException ex) {
            return markFailure(entity, ex);
        } catch (OpenRouterException ex) {
            // JSON parser throws OpenRouterException for invalid payloads
            entity.setStatut(StatutReponseAgentEnum.INVALID_RESPONSE);
            entity.setCodeErreur(GroqErrorCode.GROQ_INVALID_RESPONSE.name());
            entity.setExplication(ex.getMessage());
            return entity;
        } catch (ResourceAccessException ex) {
            entity.setStatut(StatutReponseAgentEnum.TIMEOUT);
            entity.setCodeErreur(GroqErrorCode.GROQ_TIMEOUT.name());
            entity.setFallbackReason("TIMEOUT");
            entity.setExplication(ex.getMessage());
            return entity;
        } catch (Exception ex) {
            entity.setStatut(StatutReponseAgentEnum.FAILURE);
            entity.setCodeErreur(GroqErrorCode.GROQ_UNAVAILABLE.name());
            entity.setFallbackReason("TEMPORARILY_UNAVAILABLE");
            entity.setExplication(ex.getMessage());
            log.warn("Echec agent Groq {}: {}", agent.agentKey(), ex.getMessage());
            return entity;
        }
    }

    private void applyChatSuccess(ReponseAgentIA entity, GroqChatResult chatResult) {
        String rawContent = chatResult.getRawContent();
        try {
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
            entity.setResponseHash(chatResult.getResponseHash());
            entity.setFallbackUsed(false);

            if (parsed.valid()) {
                entity.setStatut(StatutReponseAgentEnum.SUCCESS);
            } else {
                entity.setStatut(StatutReponseAgentEnum.INVALID_RESPONSE);
                entity.setCodeErreur(GroqErrorCode.GROQ_INVALID_RESPONSE.name());
            }
        } catch (OpenRouterException ex) {
            entity.setReponseBrute(rawContent);
            entity.setDureeMs(chatResult.getDurationMs());
            entity.setRetryCount(chatResult.getRetryCount());
            entity.setRequestedModelId(chatResult.getRequestedModelId());
            entity.setActualModelId(chatResult.getActualModelId());
            entity.setResponseHash(chatResult.getResponseHash());
            entity.setStatut(StatutReponseAgentEnum.INVALID_RESPONSE);
            entity.setCodeErreur(GroqErrorCode.GROQ_INVALID_RESPONSE.name());
            entity.setExplication(ex.getMessage());
        }
    }

    private ReponseAgentIA markFailure(ReponseAgentIA entity, GroqException ex) {
        entity.setCodeErreur(ex.getErrorCode().name());
        entity.setExplication(ex.getMessage());
        entity.setFallbackReason(mapFallbackReason(ex.getErrorCode()));
        entity.setStatut(switch (ex.getErrorCode()) {
            case GROQ_TIMEOUT -> StatutReponseAgentEnum.TIMEOUT;
            case MODEL_UNAVAILABLE, GROQ_MODEL_NOT_FOUND -> StatutReponseAgentEnum.MODEL_UNAVAILABLE;
            default -> StatutReponseAgentEnum.FAILURE;
        });
        return entity;
    }

    private String mapFallbackReason(GroqErrorCode code) {
        return switch (code) {
            case GROQ_RATE_LIMITED -> "RATE_LIMITED";
            case GROQ_TIMEOUT -> "TIMEOUT";
            case GROQ_UNAVAILABLE -> "TEMPORARILY_UNAVAILABLE";
            case GROQ_MODEL_NOT_FOUND, MODEL_UNAVAILABLE -> "MODEL_UNAVAILABLE";
            default -> code.name();
        };
    }

    private void recordAgentHistory(Decision decision, ReponseAgentIA entity, Utilisateur user) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("agentKey", entity.getAgentKey());
        eventData.put("provider", entity.getProvider());
        eventData.put("requestedModelId", entity.getRequestedModelId());
        eventData.put("actualModelId", entity.getActualModelId());
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
        int delayMs = groqProperties.getAgentDelayMs();
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Pause inter-agents Groq interrompue");
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

    public record GroqAnalysisBundle(
            List<ReponseAgentIA> responses,
            ConsensusResponse consensus
    ) {
    }
}
