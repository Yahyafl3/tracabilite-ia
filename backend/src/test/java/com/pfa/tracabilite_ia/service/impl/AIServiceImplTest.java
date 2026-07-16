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
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutAppelIAEnum;
import com.pfa.tracabilite_ia.repository.AppelIARepository;
import com.pfa.tracabilite_ia.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceImplTest {

    @Mock
    private AIProvider aiProvider;
    @Mock
    private OpenRouterAIProvider openRouterAIProvider;
    @Mock
    private AppelIARepository appelIARepository;
    @Mock
    private AuthService authService;

    private AIServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AIServiceImpl(
                aiProvider,
                openRouterAIProvider,
                new PromptTemplateService(),
                new SensitiveDataSanitizer(),
                appelIARepository,
                authService,
                new ObjectMapper()
        );
        MDC.put("correlationId", "corr-test-1");
    }

    @Test
    void analyzeDecision_savesTraceMetadata() {
        Utilisateur user = new Utilisateur();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@tracabilite.ia");
        user.setRole(RoleEnum.ADMINISTRATEUR);

        AIAnalysisResult analysis = new AIAnalysisResult();
        analysis.setSuggestedDecision("APPROVE");
        analysis.setConfidence(0.9);
        analysis.setRiskLevel("LOW");
        analysis.setSummary("Resume");
        analysis.setExplanation("Explication");

        when(authService.getCurrentUser()).thenReturn(user);
        when(aiProvider.getProviderName()).thenReturn("OPENROUTER");
        when(openRouterAIProvider.getDefaultModelId()).thenReturn("meta-llama/llama-3.3-70b-instruct:free");
        when(aiProvider.analyzeDecision(any(), any())).thenReturn(analysis);
        when(appelIARepository.save(any(AppelIA.class))).thenAnswer(invocation -> {
            AppelIA trace = invocation.getArgument(0);
            trace.setAppelIaId(UUID.randomUUID());
            return trace;
        });

        DecisionAnalysisRequest request = new DecisionAnalysisRequest();
        request.setPrompt("Analyser une demande");
        request.setContexte("token=secret");

        DecisionAnalysisResponse response = aiService.analyzeDecision(request);

        ArgumentCaptor<AppelIA> captor = ArgumentCaptor.forClass(AppelIA.class);
        verify(appelIARepository).save(captor.capture());

        AppelIA saved = captor.getValue();
        assertThat(saved.getStatut()).isEqualTo(StatutAppelIAEnum.SUCCESS);
        assertThat(saved.getProvider()).isEqualTo("OPENROUTER");
        assertThat(saved.getCorrelationId()).isEqualTo("corr-test-1");
        assertThat(saved.getUserPrompt()).doesNotContain("secret");
        assertThat(response.getAnalysis().getSuggestedDecision()).isEqualTo("APPROVE");
        assertThat(response.getCorrelationId()).isEqualTo("corr-test-1");
    }
}
