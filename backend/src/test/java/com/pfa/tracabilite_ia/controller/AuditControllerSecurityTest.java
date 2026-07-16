package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.config.SecurityConfig;
import com.pfa.tracabilite_ia.dto.response.AuditIntegritySummaryResponse;
import com.pfa.tracabilite_ia.filter.CorrelationIdFilter;
import com.pfa.tracabilite_ia.filter.JwtAuthenticationFilter;
import com.pfa.tracabilite_ia.jwt.JwtProvider;
import com.pfa.tracabilite_ia.security.CustomAccessDeniedHandler;
import com.pfa.tracabilite_ia.security.CustomAuthenticationEntryPoint;
import com.pfa.tracabilite_ia.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuditController.class)
@Import({
        SecurityConfig.class,
        CorrelationIdFilter.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
class AuditControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @WithMockUser(roles = "USER")
    void integritySummary_forbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/audit/integrity/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void integritySummary_allowedForAuditor() throws Exception {
        when(auditService.getIntegritySummary()).thenReturn(
                AuditIntegritySummaryResponse.builder()
                        .totalDecisions(1)
                        .validDecisions(1)
                        .invalidDecisions(0)
                        .chainIntact(true)
                        .generatedAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(get("/api/audit/integrity/summary"))
                .andExpect(status().isOk());
    }
}
