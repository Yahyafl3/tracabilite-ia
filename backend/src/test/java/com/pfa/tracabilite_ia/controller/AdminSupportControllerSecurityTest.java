package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.config.SecurityConfig;
import com.pfa.tracabilite_ia.dto.request.UpdateSupportMessageStatusRequest;
import com.pfa.tracabilite_ia.dto.response.SupportMessagePageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessageResponse;
import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import com.pfa.tracabilite_ia.exception.GlobalExceptionHandler;
import com.pfa.tracabilite_ia.filter.CorrelationIdFilter;
import com.pfa.tracabilite_ia.filter.JwtAuthenticationFilter;
import com.pfa.tracabilite_ia.jwt.JwtProvider;
import com.pfa.tracabilite_ia.security.CustomAccessDeniedHandler;
import com.pfa.tracabilite_ia.security.CustomAuthenticationEntryPoint;
import com.pfa.tracabilite_ia.service.SupportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminSupportController.class)
@Import({
        SecurityConfig.class,
        CorrelationIdFilter.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class AdminSupportControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupportService supportService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void list_unauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/admin/support/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void list_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/support/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_allowedForAdmin() throws Exception {
        when(supportService.listMessages(any(), any(), any()))
                .thenReturn(SupportMessagePageResponse.builder()
                        .content(List.of())
                        .page(0)
                        .size(10)
                        .totalElements(0)
                        .totalPages(0)
                        .last(true)
                        .build());

        mockMvc.perform(get("/api/admin/support/messages"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_allowedForAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        when(supportService.updateStatus(eq(id), any(UpdateSupportMessageStatusRequest.class)))
                .thenReturn(SupportMessageResponse.builder()
                        .id(id)
                        .status(SupportMessageStatus.CLOSED)
                        .build());

        mockMvc.perform(patch("/api/admin/support/messages/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk());

        verify(supportService).updateStatus(eq(id), any(UpdateSupportMessageStatusRequest.class));
    }
}
