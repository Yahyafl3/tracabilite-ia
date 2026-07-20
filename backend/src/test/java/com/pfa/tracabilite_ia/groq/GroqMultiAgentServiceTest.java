package com.pfa.tracabilite_ia.groq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.ai.client.GroqClient;
import com.pfa.tracabilite_ia.ai.dto.GroqChatResult;
import com.pfa.tracabilite_ia.ai.service.OpenRouterResponseParser;
import com.pfa.tracabilite_ia.ai.service.PromptTemplateService;
import com.pfa.tracabilite_ia.config.GroqProperties;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.exception.GroqErrorCode;
import com.pfa.tracabilite_ia.exception.GroqException;
import com.pfa.tracabilite_ia.openrouter.OpenRouterConsensusService;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroqMultiAgentServiceTest {

    @Mock GroqClient groqClient;
    @Mock ReponseAgentIARepository reponseAgentIARepository;
    @Mock DecisionHistoryService decisionHistoryService;
    @Mock PromptTemplateService promptTemplateService;

    private GroqProperties properties;
    private GroqMultiAgentService service;

    @BeforeEach
    void setUp() {
        properties = new GroqProperties();
        properties.setApiKey("test-key");
        properties.setAgentDelayMs(0);
        properties.setModel1("llama-3.3-70b-versatile");
        properties.setModel2("openai/gpt-oss-120b");
        properties.setModel3("openai/gpt-oss-20b");

        GroqAgentRegistryService registry = new GroqAgentRegistryService(properties, groqClient);
        ObjectMapper mapper = new ObjectMapper();
        lenient().when(promptTemplateService.systemPromptForDecisionAnalysis()).thenReturn("system");
        lenient().when(promptTemplateService.userPromptForDecisionAnalysis(anyString(), anyString()))
                .thenReturn("same-user-prompt");

        service = new GroqMultiAgentService(
                groqClient,
                registry,
                new OpenRouterConsensusService(),
                new OpenRouterResponseParser(mapper),
                promptTemplateService,
                reponseAgentIARepository,
                mapper,
                properties,
                decisionHistoryService
        );
    }

    @Test
    void analyze_callsThreeGroqModelsWithSamePrompt_andSavesProviderGroq() {
        when(groqClient.chatCompletion(eq("llama-3.3-70b-versatile"), anyString(), anyString(), anyString()))
                .thenReturn(successResult("llama-3.3-70b-versatile", "APPROVE", 0.7));
        when(groqClient.chatCompletion(eq("openai/gpt-oss-120b"), anyString(), anyString(), anyString()))
                .thenReturn(successResult("openai/gpt-oss-120b", "APPROVE", 0.8));
        when(groqClient.chatCompletion(eq("openai/gpt-oss-20b"), anyString(), anyString(), anyString()))
                .thenReturn(successResult("openai/gpt-oss-20b", "REJECT", 0.4));

        Decision decision = newDecision();
        GroqMultiAgentService.GroqAnalysisBundle bundle =
                service.analyzeDecisionAgents(decision, "prompt métier", "contexte métier");

        assertThat(bundle.responses()).hasSize(3);
        assertThat(bundle.responses()).allMatch(r -> "GROQ".equals(r.getProvider()));
        assertThat(bundle.responses().get(0).getRequestedModelId()).isEqualTo("llama-3.3-70b-versatile");
        assertThat(bundle.responses().get(1).getRequestedModelId()).isEqualTo("openai/gpt-oss-120b");
        assertThat(bundle.responses().get(2).getRequestedModelId()).isEqualTo("openai/gpt-oss-20b");
        assertThat(bundle.responses().get(0).getConfianceDeclaree()).isEqualTo(0.7);
        // 2× APPROVE vs 1× REJECT → majorité APPROUVER (consensus inchangé)
        assertThat(bundle.consensus().getDecisionConsensus()).isEqualTo("APPROUVER");
        assertThat(bundle.consensus().isConsensusAvailable()).isTrue();

        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(groqClient, times(3)).chatCompletion(anyString(), anyString(), userPromptCaptor.capture(), anyString());
        assertThat(userPromptCaptor.getAllValues()).containsOnly("same-user-prompt");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReponseAgentIA>> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(reponseAgentIARepository).saveAll(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).hasSize(3);
    }

    @Test
    void analyze_invalidJson_marksInvalidResponse() {
        GroqChatResult bad = new GroqChatResult();
        bad.setRawContent("not-json");
        bad.setRequestedModelId("llama-3.3-70b-versatile");
        bad.setActualModelId("llama-3.3-70b-versatile");
        bad.setDurationMs(10);
        when(groqClient.chatCompletion(anyString(), anyString(), anyString(), anyString())).thenReturn(bad);

        Decision decision = newDecision();
        var bundle = service.analyzeDecisionAgents(decision, "p", "c");

        assertThat(bundle.responses()).hasSize(3);
        assertThat(bundle.responses()).allMatch(r -> r.getStatut() == StatutReponseAgentEnum.INVALID_RESPONSE);
        assertThat(bundle.responses()).allMatch(r -> "GROQ".equals(r.getProvider()));
    }

    @Test
    void analyze_rateLimited_marksFailureWithoutThrowing() {
        when(groqClient.chatCompletion(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new GroqException(GroqErrorCode.GROQ_RATE_LIMITED, "Quota", 429, null, null));

        Decision decision = newDecision();
        var bundle = service.analyzeDecisionAgents(decision, "p", "c");

        assertThat(bundle.responses()).hasSize(3);
        assertThat(bundle.responses().get(0).getFallbackReason()).isEqualTo("RATE_LIMITED");
        assertThat(bundle.consensus().getDecisionConsensus()).isEqualTo("INSUFFICIENT_RESPONSES");
    }

    @Test
    void analyze_withoutApiKey_skipsAgents_keepsDecisionFlow() {
        properties.setApiKey("");
        Decision decision = newDecision();
        var bundle = service.analyzeDecisionAgents(decision, "p", "c");

        assertThat(bundle.responses()).isEmpty();
        assertThat(bundle.consensus().isConsensusAvailable()).isFalse();
        verify(groqClient, never()).chatCompletion(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void historicalOpenRouterProvider_isNotRewrittenByGroqRegistry() {
        ReponseAgentIA historical = new ReponseAgentIA();
        historical.setProvider("META_OPENROUTER");
        historical.setModelId("meta-llama/llama-3.3-70b-instruct:free");
        historical.setAgentKey("AGENT_1");
        historical.setStatut(StatutReponseAgentEnum.SUCCESS);
        historical.setDecisionProposee("APPROUVER");

        assertThat(historical.getProvider()).isEqualTo("META_OPENROUTER");
        assertThat(historical.getModelId()).isEqualTo("meta-llama/llama-3.3-70b-instruct:free");
        assertThat(GroqAgentRegistryService.PROVIDER).isEqualTo("GROQ");
        assertThat(historical.getProvider()).isNotEqualTo(GroqAgentRegistryService.PROVIDER);
    }

    private static Decision newDecision() {
        Decision decision = new Decision();
        decision.setDecisionId(UUID.randomUUID());
        decision.setStatutValidation(StatutDecisionEnum.BROUILLON);
        decision.setPrompt("prompt");
        decision.setContexte("contexte");
        return decision;
    }

    private static GroqChatResult successResult(String model, String decision, double confidence) {
        GroqChatResult result = new GroqChatResult();
        result.setRequestedModelId(model);
        result.setActualModelId(model);
        result.setDurationMs(12);
        result.setTotalTokens(40);
        result.setResponseHash("hash-" + model);
        result.setRawContent("""
                {"suggestedDecision":"%s","confidence":%s,"riskLevel":"MEDIUM","summary":"s","explanation":"e","recommendations":["r"]}
                """.formatted(decision, confidence));
        return result;
    }
}
