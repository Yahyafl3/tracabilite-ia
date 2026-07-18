package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.ValidationRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;
import com.pfa.tracabilite_ia.mapper.DecisionMapper;
import com.pfa.tracabilite_ia.mapper.ValidationMapper;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.ValidationActionRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.DecisionHashService;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import com.pfa.tracabilite_ia.service.DecisionScopeService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private ValidationActionRepository validationActionRepository;
    @Mock
    private AuthService authService;
    @Mock
    private DecisionMapper decisionMapper;
    @Mock
    private ValidationMapper validationMapper;
    @Mock
    private DecisionHistoryService decisionHistoryService;
    @Mock
    private DecisionHashService decisionHashService;
    @Mock
    private DecisionScopeService decisionScopeService;

    private ValidationServiceImpl service;
    private Utilisateur validateur;

    @BeforeEach
    void setUp() {
        service = new ValidationServiceImpl(
                decisionRepository,
                validationActionRepository,
                authService,
                decisionMapper,
                validationMapper,
                decisionHistoryService,
                decisionHashService,
                decisionScopeService
        );
        validateur = new Utilisateur();
        validateur.setId(UUID.randomUUID());
        validateur.setEmail("validateur@tracabilite.ia");
        validateur.setRole(RoleEnum.VALIDATEUR);
        when(authService.getCurrentUser()).thenReturn(validateur);
    }

    @Test
    void approuver_preservesMlAndAgentResponses() {
        UUID decisionId = UUID.randomUUID();
        Decision decision = pendingDecision(decisionId);
        decision.setSuggestedDecision("REJETER");
        decision.getReponsesAgents().add(agent(decision, "AGENT_1", "REJETER", StatutReponseAgentEnum.FAILURE));

        when(decisionScopeService.loadForValidation(decisionId)).thenReturn(decision);
        when(decisionRepository.save(any(Decision.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validationMapper.toResponseList(any())).thenReturn(List.of());
        when(decisionScopeService.loadForRead(decisionId)).thenReturn(decision);
        when(decisionMapper.toResponse(decision)).thenReturn(DecisionResponse.builder()
                .decisionId(decisionId)
                .suggestedDecision("REJETER")
                .humanFinalDecision("APPROUVER")
                .build());

        service.approuver(decisionId, new ValidationRequest());

        assertThat(decision.getSuggestedDecision()).isEqualTo("REJETER");
        assertThat(decision.getHumanDecision()).isEqualTo("APPROUVER");
        assertThat(decision.getReponsesAgents()).hasSize(1);
        assertThat(decision.getReponsesAgents().get(0).getDecisionProposee()).isEqualTo("REJETER");

        ArgumentCaptor<Decision> saved = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository).save(saved.capture());
        assertThat(saved.getValue().getDecisionId()).isEqualTo(decisionId);
    }

    @Test
    void approuver_allowedWithZeroSuccessfulAgents() {
        UUID decisionId = UUID.randomUUID();
        Decision decision = pendingDecision(decisionId);
        decision.getReponsesAgents().add(agent(decision, "AGENT_1", null, StatutReponseAgentEnum.FAILURE));
        decision.getReponsesAgents().add(agent(decision, "AGENT_2", null, StatutReponseAgentEnum.MODEL_UNAVAILABLE));
        decision.getReponsesAgents().add(agent(decision, "AGENT_3", null, StatutReponseAgentEnum.TIMEOUT));

        when(decisionScopeService.loadForValidation(decisionId)).thenReturn(decision);
        when(decisionRepository.save(any(Decision.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validationMapper.toResponseList(any())).thenReturn(List.of());
        when(decisionScopeService.loadForRead(decisionId)).thenReturn(decision);
        when(decisionMapper.toResponse(decision)).thenReturn(DecisionResponse.builder().decisionId(decisionId).build());

        DecisionResponse response = service.approuver(decisionId, new ValidationRequest());

        assertThat(response.getDecisionId()).isEqualTo(decisionId);
        verify(decisionHashService).refreshHashComponents(eq(decision), eq(decision.getReponsesAgents()));
    }

    @Test
    void modifier_setsModifiedHumanDecisionWithoutReplacingMl() {
        UUID decisionId = UUID.randomUUID();
        Decision decision = pendingDecision(decisionId);
        decision.setSuggestedDecision("APPROUVER");

        when(decisionScopeService.loadForValidation(decisionId)).thenReturn(decision);
        when(decisionRepository.save(any(Decision.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validationMapper.toResponseList(any())).thenReturn(List.of());
        when(decisionScopeService.loadForRead(decisionId)).thenReturn(decision);
        when(decisionMapper.toResponse(decision)).thenReturn(DecisionResponse.builder()
                .decisionId(decisionId)
                .suggestedDecision("APPROUVER")
                .humanFinalDecision("REJETER")
                .humanFinalAction(TypeActionEnum.MODIFIER)
                .build());

        ValidationRequest request = new ValidationRequest();
        request.setDecisionHumaine("REJETER");
        service.modifier(decisionId, request);

        assertThat(decision.getSuggestedDecision()).isEqualTo("APPROUVER");
        assertThat(decision.getHumanDecision()).isEqualTo("REJETER");
        assertThat(decision.getStatutValidation()).isEqualTo(StatutDecisionEnum.MODIFIEE);
    }

    private Decision pendingDecision(UUID decisionId) {
        Decision decision = new Decision();
        decision.setDecisionId(decisionId);
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE);
        decision.setPrompt("Test prompt");
        return decision;
    }

    private ReponseAgentIA agent(Decision decision, String key, String proposed, StatutReponseAgentEnum statut) {
        ReponseAgentIA agent = new ReponseAgentIA();
        agent.setDecision(decision);
        agent.setAgentKey(key);
        agent.setDecisionProposee(proposed);
        agent.setStatut(statut);
        return agent;
    }
}
