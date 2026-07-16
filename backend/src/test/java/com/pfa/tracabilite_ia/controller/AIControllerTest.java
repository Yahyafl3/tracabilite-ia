package com.pfa.tracabilite_ia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.request.DecisionAnalysisRequest;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import com.pfa.tracabilite_ia.dto.response.DecisionAnalysisResponse;
import com.pfa.tracabilite_ia.exception.GlobalExceptionHandler;
import com.pfa.tracabilite_ia.service.AIService;
import com.pfa.tracabilite_ia.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AIControllerTest {

    @Mock
    private AIService aiService;

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AIController controller = new AIController(aiService, authService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void analyzeDecision_returnsAnalysis() throws Exception {
        AIAnalysisResult analysis = new AIAnalysisResult();
        analysis.setSuggestedDecision("APPROVE");
        analysis.setConfidence(0.87);
        analysis.setRiskLevel("LOW");
        analysis.setSummary("Resume");
        analysis.setExplanation("Explication");

        DecisionAnalysisResponse response = new DecisionAnalysisResponse();
        response.setAnalysis(analysis);
        response.setTraceId(UUID.randomUUID());
        response.setCorrelationId("corr-1");
        response.setProvider("OPENROUTER");
        response.setModel("meta-llama/llama-3.3-70b-instruct:free");

        when(aiService.analyzeDecision(any())).thenReturn(response);

        DecisionAnalysisRequest request = new DecisionAnalysisRequest();
        request.setPrompt("Analyser une decision");

        mockMvc.perform(post("/api/ai/analyze-decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.suggestedDecision").value("APPROVE"))
                .andExpect(jsonPath("$.provider").value("OPENROUTER"));

        verify(aiService).analyzeDecision(any());
    }
}
