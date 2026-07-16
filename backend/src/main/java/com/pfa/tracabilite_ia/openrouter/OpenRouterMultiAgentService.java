package com.pfa.tracabilite_ia.openrouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.ai.client.OpenRouterClient;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.ai.dto.OpenRouterChatResult;
import com.pfa.tracabilite_ia.ai.service.OpenRouterResponseParser;
import com.pfa.tracabilite_ia.ai.service.PromptTemplateService;
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

    private final OpenRouterClient openRouterClient;
    private final OpenRouterAgentRegistryService agentRegistryService;
    private final OpenRouterConsensusService consensusService;
    private final OpenRouterResponseParser responseParser;
    private final PromptTemplateService promptTemplateService;
    private final ReponseAgentIARepository reponseAgentIARepository;
    private final ObjectMapper objectMapper;
    private final OpenRouterProperties openRouterProperties;
    private final DecisionHistoryService decisionHistoryService;

    public OpenRouterMultiAgentService(OpenRouterClient openRouterClient,
                                       OpenRouterAgentRegistryService agentRegistryService,
                                       OpenRouterConsensusService consensusService,
                                       OpenRouterResponseParser responseParser,
                                       PromptTemplateService promptTemplateService,
                                       ReponseAgentIARepository reponseAgentIARepository,
                                       ObjectMapper objectMapper,
                                       OpenRouterProperties openRouterProperties,
                                       DecisionHistoryService decisionHistoryService) {
        this.openRouterClient = openRouterClient;
        this.agentRegistryService = agentRegistryService;
        this.consensusService = consensusService;
        this.responseParser = responseParser;
        this.promptTemplateService = promptTemplateService;
        this.reponseAgentIARepository = reponseAgentIARepository;
        this.objectMapper = objectMapper;
        this.openRouterProperties = openRouterProperties;
        this.decisionHistoryService = decisionHistoryService;
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

        String systemPrompt = promptTemplateService.systemPromptForDecisionAnalysis();
        String userPrompt = promptTemplateService.userPromptForDecisionAnalysis(prompt, contexte);
        String correlationId = currentCorrelationId();

        Set<String> availableModelIds = fetchAvailableModelIdsSafely();
        List<ReponseAgentIA> responses = new ArrayList<>();

        List<OpenRouterAgentDefinition> agents = agentRegistryService.configuredAgents();
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
        eventData.put("modelId", entity.getModelId());
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
        ReponseAgentIA entity = new ReponseAgentIA();
        entity.setDecision(decision);
        entity.setAgentKey(agent.agentKey());
        entity.setModelId(agent.modelId());
        entity.setModelName(agent.displayName());
        entity.setProvider(agent.provider());

        if (!agentRegistryService.isAgentAvailable(agent.agentKey(), availableModelIds)) {
            entity.setStatut(StatutReponseAgentEnum.MODEL_UNAVAILABLE);
            entity.setCodeErreur(OpenRouterErrorCode.MODEL_UNAVAILABLE.name());
            log.warn("Agent {} indisponible pour modelId={}", agent.agentKey(), agent.modelId());
            return entity;
        }

        try {
            OpenRouterChatResult chatResult = openRouterClient.chatCompletion(
                    agent.modelId(), systemPrompt, userPrompt, correlationId
            );
            AIAnalysisResult parsed = responseParser.parse(chatResult.getRawContent());

            entity.setReponseBrute(chatResult.getRawContent());
            entity.setReponseNormalisee(responseParser.toNormalizedJson(parsed));
            entity.setDecisionProposee(parsed.getSuggestedDecision());
            entity.setConfianceDeclaree(parsed.getConfidence());
            entity.setNiveauRisque(parsed.getRiskLevel());
            entity.setResume(parsed.getSummary());
            entity.setExplication(parsed.getExplanation());
            entity.setRecommandationsJson(writeJson(parsed.getRecommendations()));
            entity.setDureeMs(chatResult.getDurationMs());
            entity.setNombreTokens(chatResult.getTotalTokens());
            entity.setStatut(StatutReponseAgentEnum.SUCCESS);
            return entity;
        } catch (OpenRouterException ex) {
            return markFailure(entity, ex);
        } catch (ResourceAccessException ex) {
            entity.setStatut(StatutReponseAgentEnum.TIMEOUT);
            entity.setCodeErreur(OpenRouterErrorCode.OPENROUTER_TIMEOUT.name());
            entity.setExplication(ex.getMessage());
            return entity;
        } catch (Exception ex) {
            entity.setStatut(StatutReponseAgentEnum.FAILURE);
            entity.setCodeErreur(OpenRouterErrorCode.OPENROUTER_UNAVAILABLE.name());
            entity.setExplication(ex.getMessage());
            log.warn("Echec agent {}: {}", agent.agentKey(), ex.getMessage());
            return entity;
        }
    }

    private ReponseAgentIA markFailure(ReponseAgentIA entity, OpenRouterException ex) {
        entity.setCodeErreur(ex.getErrorCode().name());
        entity.setExplication(ex.getMessage());
        entity.setStatut(switch (ex.getErrorCode()) {
            case OPENROUTER_TIMEOUT -> StatutReponseAgentEnum.TIMEOUT;
            case MODEL_UNAVAILABLE -> StatutReponseAgentEnum.MODEL_UNAVAILABLE;
            default -> StatutReponseAgentEnum.FAILURE;
        });
        return entity;
    }

    private Set<String> fetchAvailableModelIdsSafely() {
        try {
            return agentRegistryService.fetchAvailableModelIds();
        } catch (Exception ex) {
            log.warn("Impossible de verifier la disponibilite OpenRouter: {}", ex.getMessage());
            return Set.of();
        }
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

    public record OpenRouterAnalysisBundle(
            List<ReponseAgentIA> responses,
            ConsensusResponse consensus
    ) {
    }
}
