package com.pfa.tracabilite_ia.ai.provider;

import com.pfa.tracabilite_ia.ai.client.OpenRouterClient;
import com.pfa.tracabilite_ia.ai.dto.OpenRouterChatResult;
import com.pfa.tracabilite_ia.ai.service.OpenRouterResponseParser;
import com.pfa.tracabilite_ia.ai.service.PromptTemplateService;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class OpenRouterAIProvider implements AIProvider {

    private static final String CORRELATION_ID_KEY = "correlationId";

    private final OpenRouterClient openRouterClient;
    private final OpenRouterResponseParser responseParser;
    private final PromptTemplateService promptTemplateService;
    private final OpenRouterProperties properties;

    public OpenRouterAIProvider(OpenRouterClient openRouterClient,
                                OpenRouterResponseParser responseParser,
                                PromptTemplateService promptTemplateService,
                                OpenRouterProperties properties) {
        this.openRouterClient = openRouterClient;
        this.responseParser = responseParser;
        this.promptTemplateService = promptTemplateService;
        this.properties = properties;
    }

    @Override
    public String getProviderName() {
        return "OPENROUTER";
    }

    @Override
    public boolean isAvailable() {
        return properties.isConfigured();
    }

    @Override
    public AIAnalysisResult analyzeDecision(String prompt, String contexte) {
        OpenRouterChatResult chatResult = openRouterClient.chatCompletion(
                properties.getModel1(),
                promptTemplateService.systemPromptForDecisionAnalysis(),
                promptTemplateService.userPromptForDecisionAnalysis(prompt, contexte),
                currentCorrelationId()
        );
        return responseParser.parse(chatResult.getRawContent()).requireValid();
    }

    @Override
    public String summarizeContext(String contexte) {
        return analyzeDecision("Resumer le contexte", contexte).getSummary();
    }

    @Override
    public String evaluateRisk(String prompt, String contexte) {
        return analyzeDecision(prompt, contexte).getRiskLevel();
    }

    @Override
    public String generateRecommendation(String prompt, String contexte) {
        AIAnalysisResult result = analyzeDecision(prompt, contexte);
        if (result.getRecommendations() != null && !result.getRecommendations().isEmpty()) {
            return result.getRecommendations().get(0);
        }
        return result.getSuggestedDecision();
    }

    @Override
    public String generateExplanation(String prompt, String contexte) {
        return analyzeDecision(prompt, contexte).getExplanation();
    }

    public String getDefaultModelId() {
        return properties.getModel1();
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        return correlationId != null ? correlationId : "unknown";
    }
}
