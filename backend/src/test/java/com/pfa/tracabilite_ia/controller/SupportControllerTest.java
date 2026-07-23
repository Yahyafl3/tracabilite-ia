package com.pfa.tracabilite_ia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.config.SecurityConfig;
import com.pfa.tracabilite_ia.dto.request.CreateSupportMessageRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupportController.class)
@Import({
        SecurityConfig.class,
        CorrelationIdFilter.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupportService supportService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void createMessage_publicWithoutJwt_returnsCreated() throws Exception {
        when(supportService.createMessage(any(), anyString()))
                .thenReturn(new MessageResponse(
                        "Votre demande a été envoyée. Notre équipe vous répondra dès que possible."));

        CreateSupportMessageRequest body = validBody();

        mockMvc.perform(post("/api/support/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(
                        "Votre demande a été envoyée. Notre équipe vous répondra dès que possible."));
    }

    @Test
    void createMessage_blankName_returnsBadRequest() throws Exception {
        CreateSupportMessageRequest body = validBody();
        body.setName("");

        mockMvc.perform(post("/api/support/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMessage_invalidEmail_returnsBadRequest() throws Exception {
        CreateSupportMessageRequest body = validBody();
        body.setEmail("not-an-email");

        mockMvc.perform(post("/api/support/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMessage_subjectTooShort_returnsBadRequest() throws Exception {
        CreateSupportMessageRequest body = validBody();
        body.setSubject("ab");

        mockMvc.perform(post("/api/support/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMessage_messageTooShort_returnsBadRequest() throws Exception {
        CreateSupportMessageRequest body = validBody();
        body.setMessage("tropcour"); // 8 chars < 10

        mockMvc.perform(post("/api/support/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMessage_messageTooLong_returnsBadRequest() throws Exception {
        CreateSupportMessageRequest body = validBody();
        body.setMessage("x".repeat(2001));

        mockMvc.perform(post("/api/support/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    private static CreateSupportMessageRequest validBody() {
        CreateSupportMessageRequest body = new CreateSupportMessageRequest();
        body.setName("Jane Doe");
        body.setEmail("user@example.com");
        body.setSubject("Problème de connexion");
        body.setMessage("Je n'arrive pas à me connecter depuis ce matin.");
        return body;
    }
}
