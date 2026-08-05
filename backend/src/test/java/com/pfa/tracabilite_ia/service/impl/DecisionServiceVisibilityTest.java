package com.pfa.tracabilite_ia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.groq.GroqMultiAgentService;
import com.pfa.tracabilite_ia.mapper.DecisionMapper;
import com.pfa.tracabilite_ia.mapper.ValidationMapper;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRetryService;
import com.pfa.tracabilite_ia.repository.CreditDecisionDataRepository;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.EducationDecisionDataRepository;
import com.pfa.tracabilite_ia.repository.MedicalDecisionDataRepository;
import com.pfa.tracabilite_ia.repository.SystemeIARepository;
import com.pfa.tracabilite_ia.repository.ValidationActionRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import com.pfa.tracabilite_ia.service.DecisionScopeService;
import com.pfa.tracabilite_ia.service.DecisionSourceService;
import com.pfa.tracabilite_ia.service.DecisionHashService;
import com.pfa.tracabilite_ia.service.MLDecisionService;
import com.pfa.tracabilite_ia.validation.CreditFeaturesValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionServiceVisibilityTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private SystemeIARepository systemeIARepository;
    @Mock
    private ValidationActionRepository validationActionRepository;
    @Mock
    private MLDecisionService mlDecisionService;
    @Mock
    private GroqMultiAgentService groqMultiAgentService;
    @Mock
    private OpenRouterAgentRetryService openRouterAgentRetryService;
    @Mock
    private DecisionMapper decisionMapper;
    @Mock
    private ValidationMapper validationMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CreditFeaturesValidator creditFeaturesValidator;
    @Mock
    private DecisionHistoryService decisionHistoryService;
    @Mock
    private DecisionSourceService decisionSourceService;
    @Mock
    private DecisionHashService decisionHashService;
    @Mock
    private AuthService authService;
    @Mock
    private DecisionScopeService decisionScopeService;
    @Mock
    private CreditDecisionDataRepository creditDecisionDataRepository;
    @Mock
    private MedicalDecisionDataRepository medicalDecisionDataRepository;
    @Mock
    private EducationDecisionDataRepository educationDecisionDataRepository;

    @InjectMocks
    private DecisionServiceImpl service;

    @BeforeEach
    void setUp() {
        when(decisionMapper.toResponseList(any())).thenAnswer(invocation -> ((List<Decision>) invocation.getArgument(0)).stream().map(this::toResponse).toList());
    }

    @Test
    void rechercher_forAdmin_passesNoScopeRestriction() {
        Utilisateur admin = user(RoleEnum.ADMINISTRATEUR, "admin@example.com");
        when(authService.getCurrentUser()).thenReturn(admin);
        when(decisionRepository.searchFiltered(anyString(), any(), any(), any(), any(), anyString(),
                any(LocalDateTime.class), any(LocalDateTime.class), anyString(), anyString(), any(), any(Pageable.class)))
                .thenReturn(pageOf(new Decision()));

        service.rechercher("recherche", null, null, null, null, null, null, null, 0, 10);

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(decisionRepository).searchFiltered(eq("recherche"), isNull(), isNull(), isNull(), isNull(), eq(""),
                any(LocalDateTime.class), any(LocalDateTime.class), roleCaptor.capture(), emailCaptor.capture(), idCaptor.capture(), any(Pageable.class));

        assertThat(roleCaptor.getValue()).isEqualTo("ADMINISTRATEUR");
        assertThat(emailCaptor.getValue()).isEqualTo("admin@example.com");
        assertThat(idCaptor.getValue()).isEqualTo(admin.getId());
    }

    @Test
    void rechercher_forCreditAnalyst_appliesCreditOwnershipScope() {
        Utilisateur analyste = user(RoleEnum.UTILISATEUR, "analyste@example.com");
        when(authService.getCurrentUser()).thenReturn(analyste);
        when(decisionRepository.searchFiltered(anyString(), any(), any(), any(), any(), anyString(),
                any(LocalDateTime.class), any(LocalDateTime.class), anyString(), anyString(), any(), any(Pageable.class)))
                .thenReturn(pageOf(new Decision()));

        service.rechercher("recherche", null, DecisionDomain.CREDIT, null, null, null, null, null, 0, 10);

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DecisionDomain> domainCaptor = ArgumentCaptor.forClass(DecisionDomain.class);
        verify(decisionRepository).searchFiltered(eq("recherche"), isNull(), domainCaptor.capture(), isNull(), isNull(), eq(""),
                any(LocalDateTime.class), any(LocalDateTime.class), roleCaptor.capture(), anyString(), any(UUID.class), any(Pageable.class));

        assertThat(roleCaptor.getValue()).isEqualTo("UTILISATEUR");
        assertThat(domainCaptor.getValue()).isEqualTo(DecisionDomain.CREDIT);
    }

    @Test
    void obtenir_forUnauthorizedDecision_throwsAccessDenied() {
        UUID id = UUID.randomUUID();
        Decision decision = new Decision();
        decision.setDecisionId(id);
        decision.setDomaine(DecisionDomain.MEDICAL);
        decision.setCreatedBy("other@example.com");

        Utilisateur analyste = user(RoleEnum.UTILISATEUR, "analyste@example.com");
        when(authService.getCurrentUser()).thenReturn(analyste);
        when(decisionRepository.findByIdWithFactors(id)).thenReturn(Optional.of(decision));

        assertThatThrownBy(() -> service.obtenir(id))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static Utilisateur user(RoleEnum role, String email) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setEmail(email);
        utilisateur.setRole(role);
        return utilisateur;
    }

    private static Page<Decision> pageOf(Decision decision) {
        return new PageImpl<>(List.of(decision));
    }

    private Object toResponse(Decision decision) {
        return new com.pfa.tracabilite_ia.dto.response.DecisionResponse();
    }
}
