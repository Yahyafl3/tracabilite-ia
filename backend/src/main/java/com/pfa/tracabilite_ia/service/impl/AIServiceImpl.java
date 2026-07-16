package com.pfa.tracabilite_ia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.ai.provider.AIProvider;
import com.pfa.tracabilite_ia.ai.provider.OpenRouterAIProvider;
import com.pfa.tracabilite_ia.ai.service.PromptTemplateService;
import com.pfa.tracabilite_ia.ai.service.SensitiveDataSanitizer;
import com.pfa.tracabilite_ia.dto.request.DecisionAnalysisRequest;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import com.pfa.tracabilite_ia.dto.response.DecisionAnalysisResponse;
import com.pfa.tracabilite_ia.entities.AppelIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.StatutAppelIAEnum;
import com.pfa.tracabilite_ia.exception.AIServiceException;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import com.pfa.tracabilite_ia.repository.AppelIARepository;
import com.pfa.tracabilite_ia.service.AIService;
import com.pfa.tracabilite_ia.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
public class AIServiceImpl implements AIService {

    private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final AIProvider aiProvider;
    private final OpenRouterAIProvider openRouterAIProvider;
    private final PromptTemplateService promptTemplateService;
    private final SensitiveDataSanitizer sanitizer;
    private final AppelIARepository appelIARepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AIServiceImpl(AIProvider aiProvider,
                         OpenRouterAIProvider openRouterAIProvider,
                         PromptTemplateService promptTemplateService,
                         SensitiveDataSanitizer sanitizer,
                         AppelIARepository appelIARepository,
                         AuthService authService,
                         ObjectMapper objectMapper) {
        this.aiProvider = aiProvider;
        this.openRouterAIProvider = openRouterAIProvider;
        this.promptTemplateService = promptTemplateService;
        this.sanitizer = sanitizer;
        this.appelIARepository = appelIARepository;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public DecisionAnalysisResponse analyzeDecision(DecisionAnalysisRequest request) {
        String correlationId = currentCorrelationId();
        Utilisateur utilisateur = authService.getCurrentUser();
        String sanitizedPrompt = sanitizer.sanitize(request.getPrompt());
        String sanitizedContext = sanitizer.sanitize(request.getContexte());

        AppelIA trace = new AppelIA();
        trace.setProvider(aiProvider.getProviderName());
        trace.setModel(openRouterAIProvider.getDefaultModelId());
        trace.setModelVersion(openRouterAIProvider.getDefaultModelId());
        trace.setSystemPrompt(promptTemplateService.systemPromptForDecisionAnalysis());
        trace.setUserPrompt(promptTemplateService.userPromptForDecisionAnalysis(sanitizedPrompt, sanitizedContext));
        trace.setCorrelationId(correlationId);
        trace.setUtilisateur(utilisateur);

        long startedAt = System.currentTimeMillis();
        try {
            AIAnalysisResult analysis = aiProvider.analyzeDecision(sanitizedPrompt, sanitizedContext);
            trace.setDurationMs(System.currentTimeMillis() - startedAt);
            trace.setStatut(StatutAppelIAEnum.SUCCESS);
            trace.setResponse(objectMapper.writeValueAsString(analysis));
            AppelIA saved = appelIARepository.save(trace);

            DecisionAnalysisResponse response = new DecisionAnalysisResponse();
            response.setAnalysis(analysis);
            response.setTraceId(saved.getAppelIaId());
            response.setCorrelationId(correlationId);
            response.setProvider(saved.getProvider());
            response.setModel(saved.getModel());
            return response;
        } catch (ResourceAccessException ex) {
            saveFailure(trace, startedAt, StatutAppelIAEnum.TIMEOUT, "Timeout OpenRouter");
            throw new OpenRouterException(
                    com.pfa.tracabilite_ia.exception.OpenRouterErrorCode.OPENROUTER_TIMEOUT,
                    "OpenRouter indisponible ou timeout",
                    0,
                    ex
            );
        } catch (OpenRouterException ex) {
            saveFailure(trace, startedAt, mapStatut(ex), ex.getErrorCode().name());
            throw ex;
        } catch (Exception ex) {
            saveFailure(trace, startedAt, StatutAppelIAEnum.FAILURE, "Erreur inattendue lors de l'analyse IA");
            log.error("Erreur analyse IA correlationId={}", correlationId, ex);
            throw new AIServiceException("Erreur lors de l'analyse IA", ex);
        }
    }

    @Override
    public String summarizeContext(String contexte) {
        return aiProvider.summarizeContext(sanitizer.sanitize(contexte));
    }

    @Override
    public String evaluateRisk(DecisionAnalysisRequest request) {
        return aiProvider.evaluateRisk(
                sanitizer.sanitize(request.getPrompt()),
                sanitizer.sanitize(request.getContexte())
        );
    }

    @Override
    public String generateRecommendation(DecisionAnalysisRequest request) {
        return aiProvider.generateRecommendation(
                sanitizer.sanitize(request.getPrompt()),
                sanitizer.sanitize(request.getContexte())
        );
    }

    @Override
    public String generateExplanation(DecisionAnalysisRequest request) {
        return aiProvider.generateExplanation(
                sanitizer.sanitize(request.getPrompt()),
                sanitizer.sanitize(request.getContexte())
        );
    }

    private StatutAppelIAEnum mapStatut(OpenRouterException ex) {
        return ex.getErrorCode() == com.pfa.tracabilite_ia.exception.OpenRouterErrorCode.OPENROUTER_TIMEOUT
                ? StatutAppelIAEnum.TIMEOUT
                : StatutAppelIAEnum.FAILURE;
    }

    private void saveFailure(AppelIA trace, long startedAt, StatutAppelIAEnum statut, String message) {
        trace.setDurationMs(System.currentTimeMillis() - startedAt);
        trace.setStatut(statut);
        trace.setErrorMessage(message);
        appelIARepository.save(trace);
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        return correlationId != null ? correlationId : "unknown";
    }
}
