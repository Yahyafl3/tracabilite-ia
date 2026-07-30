package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.config.SecurityConfig;
import com.pfa.tracabilite_ia.exception.GlobalExceptionHandler;
import com.pfa.tracabilite_ia.filter.CorrelationIdFilter;
import com.pfa.tracabilite_ia.filter.JwtAuthenticationFilter;
import com.pfa.tracabilite_ia.jwt.JwtProvider;
import com.pfa.tracabilite_ia.security.CustomAccessDeniedHandler;
import com.pfa.tracabilite_ia.security.CustomAuthenticationEntryPoint;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.DecisionExportService;
import com.pfa.tracabilite_ia.service.DecisionService;
import com.pfa.tracabilite_ia.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DecisionController.class)
@Import({
        SecurityConfig.class,
        CorrelationIdFilter.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class DecisionControllerUserSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DecisionService decisionService;

    @MockitoBean
    private ValidationService validationService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private DecisionExportService decisionExportService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @WithMockUser(roles = "USER")
    void approve_forbiddenForCreditAgent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/decisions/{id}/approve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void reject_forbiddenForCreditAgent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/decisions/{id}/reject", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void modify_forbiddenForCreditAgent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/decisions/{id}/modify", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentaire\":\"x\",\"decisionHumaine\":\"APPROUVER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void retryFailedAgents_forbiddenForCreditAgent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/decisions/{id}/retry-failed-agents", id))
                .andExpect(status().isForbidden());
    }
}
