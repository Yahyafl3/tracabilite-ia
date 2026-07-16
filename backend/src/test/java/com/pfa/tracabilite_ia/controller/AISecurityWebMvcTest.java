package com.pfa.tracabilite_ia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.config.SecurityConfig;
import com.pfa.tracabilite_ia.dto.request.DecisionAnalysisRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionAnalysisResponse;
import com.pfa.tracabilite_ia.filter.CorrelationIdFilter;
import com.pfa.tracabilite_ia.filter.JwtAuthenticationFilter;
import com.pfa.tracabilite_ia.jwt.JwtProvider;
import com.pfa.tracabilite_ia.security.CustomAccessDeniedHandler;
import com.pfa.tracabilite_ia.security.CustomAuthenticationEntryPoint;
import com.pfa.tracabilite_ia.service.AIService;
import com.pfa.tracabilite_ia.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AIController.class)
@Import({
        SecurityConfig.class,
        CorrelationIdFilter.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
class AISecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AIService aiService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void ping_withoutToken_returns200() throws Exception {
        mockMvc.perform(get("/api/ai/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void testPost_withoutToken_returns200() throws Exception {
        mockMvc.perform(post("/api/ai/test-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hello\":\"world\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void analyzeDecision_withoutToken_returns401() throws Exception {
        DecisionAnalysisRequest request = new DecisionAnalysisRequest();
        request.setPrompt("Test");

        mockMvc.perform(post("/api/ai/analyze-decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analyzeDecision_withAuditorRole_returns403() throws Exception {
        when(jwtProvider.validateToken("auditor-token")).thenReturn(true);
        when(jwtProvider.getUserId("auditor-token")).thenReturn("auditor-id");
        when(jwtProvider.getRole("auditor-token")).thenReturn("AUDITEUR");

        DecisionAnalysisRequest request = new DecisionAnalysisRequest();
        request.setPrompt("Test");

        mockMvc.perform(post("/api/ai/analyze-decision")
                        .header("Authorization", "Bearer auditor-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void analyzeDecision_withAdminRole_returns200() throws Exception {
        when(jwtProvider.validateToken("admin-token")).thenReturn(true);
        when(jwtProvider.getUserId("admin-token")).thenReturn("admin-id");
        when(jwtProvider.getRole("admin-token")).thenReturn("ADMINISTRATEUR");
        when(aiService.analyzeDecision(any())).thenReturn(new DecisionAnalysisResponse());

        DecisionAnalysisRequest request = new DecisionAnalysisRequest();
        request.setPrompt("Test");

        mockMvc.perform(post("/api/ai/analyze-decision")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void analyzeDecision_withUserRole_returns200() throws Exception {
        when(jwtProvider.validateToken("user-token")).thenReturn(true);
        when(jwtProvider.getUserId("user-token")).thenReturn("user-id");
        when(jwtProvider.getRole("user-token")).thenReturn("USER");
        when(aiService.analyzeDecision(any())).thenReturn(new DecisionAnalysisResponse());

        DecisionAnalysisRequest request = new DecisionAnalysisRequest();
        request.setPrompt("Test");

        mockMvc.perform(post("/api/ai/analyze-decision")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
