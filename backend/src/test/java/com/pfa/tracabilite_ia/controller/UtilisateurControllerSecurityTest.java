package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.config.SecurityConfig;
import com.pfa.tracabilite_ia.exception.GlobalExceptionHandler;
import com.pfa.tracabilite_ia.filter.CorrelationIdFilter;
import com.pfa.tracabilite_ia.filter.JwtAuthenticationFilter;
import com.pfa.tracabilite_ia.jwt.JwtProvider;
import com.pfa.tracabilite_ia.security.CustomAccessDeniedHandler;
import com.pfa.tracabilite_ia.security.CustomAuthenticationEntryPoint;
import com.pfa.tracabilite_ia.service.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UtilisateurController.class)
@Import({
        SecurityConfig.class,
        CorrelationIdFilter.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class UtilisateurControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilisateurService utilisateurService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void list_unauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void list_forbiddenForCreditAgent() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VALIDATOR")
    void list_forbiddenForValidator() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void list_forbiddenForAuditor() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_allowedForAdmin() throws Exception {
        when(utilisateurService.lister()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }
}
