package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.DomainValidationRequest;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.mapper.DecisionMapper;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionOrchestratorValidationTest {

    @Mock DecisionRepository decisionRepository;
    @Mock MLDecisionService mlDecisionService;
    @Mock AuthService authService;
    @Mock AuditLogService auditLogService;
    @Mock DecisionMapper decisionMapper;

    DecisionOrchestratorService orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new DecisionOrchestratorService(
                decisionRepository,
                mlDecisionService, authService, auditLogService, decisionMapper, new ObjectMapper(),
                (decision, domain, featuresJson, user) -> false
        );
    }

    @Test
    void authorCannotValidateOwnDossier() {
        UUID id = UUID.randomUUID();
        Decision decision = new Decision();
        decision.setDecisionId(id);
        decision.setDomaine(DecisionDomain.CREDIT);
        decision.setCreatedBy("author@test.com");
        decision.setSuggestedDecision("RISQUE_FAIBLE");
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);

        Utilisateur author = new Utilisateur();
        author.setId(UUID.randomUUID());
        author.setEmail("author@test.com");
        author.setRole(RoleEnum.RESPONSABLE_CREDIT);

        when(authService.getCurrentUser()).thenReturn(author);
        when(decisionRepository.findById(id)).thenReturn(Optional.of(decision));

        DomainValidationRequest req = new DomainValidationRequest();
        req.setDecisionFinale("ACCEPTEE");
        req.setJustificationHumaine("Justification de validation suffisante");

        assertThrows(ResponseStatusException.class, () -> orchestrator.validate(id, req));
    }

    @Test
    void unauthorizedRoleCannotValidateMedical() {
        UUID id = UUID.randomUUID();
        Decision decision = new Decision();
        decision.setDecisionId(id);
        decision.setDomaine(DecisionDomain.MEDICAL);
        decision.setCreatedBy("other@test.com");
        decision.setSuggestedDecision("RISQUE_ELEVE");
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);

        Utilisateur creditOnly = new Utilisateur();
        creditOnly.setId(UUID.randomUUID());
        creditOnly.setEmail("credit@test.com");
        creditOnly.setRole(RoleEnum.RESPONSABLE_CREDIT);

        when(authService.getCurrentUser()).thenReturn(creditOnly);
        when(decisionRepository.findById(id)).thenReturn(Optional.of(decision));

        DomainValidationRequest req = new DomainValidationRequest();
        req.setDecisionFinale("SUIVI_STANDARD");
        req.setJustificationHumaine("Justification de validation suffisante");

        assertThrows(ResponseStatusException.class, () -> orchestrator.validate(id, req));
    }

    @Test
    void disagreementRequiresDetailedJustification() {
        UUID id = UUID.randomUUID();
        Decision decision = new Decision();
        decision.setDecisionId(id);
        decision.setDomaine(DecisionDomain.CREDIT);
        decision.setCreatedBy("agent@test.com");
        decision.setSuggestedDecision("RISQUE_ELEVE");
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);

        Utilisateur validator = new Utilisateur();
        validator.setId(UUID.randomUUID());
        validator.setEmail("validator@test.com");
        validator.setRole(RoleEnum.RESPONSABLE_CREDIT);

        when(authService.getCurrentUser()).thenReturn(validator);
        when(decisionRepository.findById(id)).thenReturn(Optional.of(decision));

        DomainValidationRequest req = new DomainValidationRequest();
        req.setDecisionFinale("ACCEPTEE");
        req.setAccordAvecIa(false);
        req.setJustificationHumaine("trop court");

        assertThrows(ResponseStatusException.class, () -> orchestrator.validate(id, req));
    }

    @Test
    void invalidFinalDecisionForDomainRejected() {
        UUID id = UUID.randomUUID();
        Decision decision = new Decision();
        decision.setDecisionId(id);
        decision.setDomaine(DecisionDomain.EDUCATION);
        decision.setCreatedBy("agent@test.com");
        decision.setSuggestedDecision("RISQUE_MOYEN");
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);

        Utilisateur validator = new Utilisateur();
        validator.setId(UUID.randomUUID());
        validator.setEmail("peda@test.com");
        validator.setRole(RoleEnum.RESPONSABLE_PEDAGOGIQUE);

        when(authService.getCurrentUser()).thenReturn(validator);
        when(decisionRepository.findById(id)).thenReturn(Optional.of(decision));

        DomainValidationRequest req = new DomainValidationRequest();
        req.setDecisionFinale("ACCEPTEE");
        req.setJustificationHumaine("Justification de validation suffisante");

        assertThrows(ResponseStatusException.class, () -> orchestrator.validate(id, req));
    }
}
