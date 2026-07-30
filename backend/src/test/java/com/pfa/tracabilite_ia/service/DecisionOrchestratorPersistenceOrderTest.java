package com.pfa.tracabilite_ia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.request.CreateCreditDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateEducationDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateMedicalDecisionRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.dto.response.DomainPredictionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.mapper.DecisionMapper;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionOrchestratorPersistenceOrderTest {

    @Mock DecisionRepository decisionRepository;
    @Mock MLDecisionService mlDecisionService;
    @Mock AuthService authService;
    @Mock AuditLogService auditLogService;
    @Mock DecisionMapper decisionMapper;
    @Mock DomainAgentConsultationService domainAgentConsultationService;

    DecisionOrchestratorService orchestrator;
    Utilisateur agent;

    @BeforeEach
    void setUp() {
        orchestrator = new DecisionOrchestratorService(
                decisionRepository,
                mlDecisionService,
                authService,
                auditLogService,
                decisionMapper,
                new ObjectMapper(),
                domainAgentConsultationService
        );
        agent = new Utilisateur();
        agent.setId(UUID.randomUUID());
        agent.setEmail("user@tracabilite.ia");
        agent.setRole(RoleEnum.UTILISATEUR);
        when(authService.getCurrentUser()).thenReturn(agent);
        when(decisionMapper.toResponse(any(Decision.class))).thenAnswer(inv -> {
            Decision d = inv.getArgument(0);
            DecisionResponse r = new DecisionResponse();
            r.setDecisionId(d.getDecisionId());
            r.setDomaine(d.getDomaine() != null ? d.getDomaine().name() : null);
            return r;
        });
    }

    @Test
    void createEducation_persistsRootBeforeAgents_andAuditsAfter() {
        when(mlDecisionService.predictEducation(any())).thenReturn(samplePrediction("EDUCATION"));
        stubSaveAssignsId();

        CreateEducationDecisionRequest req = sampleEducation(true);
        DecisionResponse response = orchestrator.createAndAnalyzeEducation(req);

        assertNotNull(response.getDecisionId());
        InOrder order = inOrder(decisionRepository, domainAgentConsultationService, auditLogService);
        order.verify(decisionRepository).save(argThat(d ->
                d.getEducationData() != null
                        && d.getEducationData().getDecision() == d
                        && d.getDomaine() == DecisionDomain.EDUCATION));
        order.verify(domainAgentConsultationService).consultAgents(
                any(Decision.class), eq(DecisionDomain.EDUCATION), any(), eq(agent));
        order.verify(decisionRepository, atLeastOnce()).save(any(Decision.class));
        order.verify(auditLogService).record(any(UUID.class), eq(agent), eq("ANALYSE"), any(), any(), any(), any());
    }

    @Test
    void createEducation_includeAgentsFalse_skipsAgents() {
        when(mlDecisionService.predictEducation(any())).thenReturn(samplePrediction("EDUCATION"));
        stubSaveAssignsId();

        orchestrator.createAndAnalyzeEducation(sampleEducation(false));

        verify(domainAgentConsultationService, never()).consultAgents(any(), any(), any(), any());
        verify(decisionRepository, times(1)).save(any(Decision.class));
        verify(auditLogService).record(any(UUID.class), eq(agent), eq("ANALYSE"), any(), any(), any(), any());
    }

    @Test
    void createCredit_wiresBidirectionalOneToOne_beforeSave() {
        when(mlDecisionService.predictCredit(any())).thenReturn(samplePrediction("CREDIT"));
        AtomicReference<Decision> captured = new AtomicReference<>();
        when(decisionRepository.save(any(Decision.class))).thenAnswer(inv -> {
            Decision d = inv.getArgument(0);
            if (d.getDecisionId() == null) {
                d.setDecisionId(UUID.randomUUID());
            }
            captured.set(d);
            return d;
        });

        CreateCreditDecisionRequest req = sampleCredit(false);
        orchestrator.createAndAnalyzeCredit(req);

        Decision d = captured.get();
        assertNotNull(d.getCreditData());
        assertSame(d, d.getCreditData().getDecision());
        assertEquals(DecisionDomain.CREDIT, d.getDomaine());
        verify(domainAgentConsultationService, never()).consultAgents(any(), any(), any(), any());
    }

    @Test
    void createMedical_wiresBidirectionalOneToOne_beforeSave() {
        when(mlDecisionService.predictMedical(any())).thenReturn(samplePrediction("MEDICAL"));
        AtomicReference<Decision> captured = new AtomicReference<>();
        when(decisionRepository.save(any(Decision.class))).thenAnswer(inv -> {
            Decision d = inv.getArgument(0);
            if (d.getDecisionId() == null) {
                d.setDecisionId(UUID.randomUUID());
            }
            captured.set(d);
            return d;
        });

        orchestrator.createAndAnalyzeMedical(sampleMedical(false));

        Decision d = captured.get();
        assertNotNull(d.getMedicalData());
        assertSame(d, d.getMedicalData().getDecision());
        assertEquals(DecisionDomain.MEDICAL, d.getDomaine());
    }

    @Test
    void createCredit_includeAgentsTrue_callsAgentsAfterFirstSave() {
        when(mlDecisionService.predictCredit(any())).thenReturn(samplePrediction("CREDIT"));
        stubSaveAssignsId();

        orchestrator.createAndAnalyzeCredit(sampleCredit(true));

        InOrder order = inOrder(decisionRepository, domainAgentConsultationService);
        order.verify(decisionRepository).save(any(Decision.class));
        order.verify(domainAgentConsultationService).consultAgents(
                any(Decision.class), eq(DecisionDomain.CREDIT), any(), eq(agent));
    }

    @Test
    void createMedical_includeAgentsTrue_callsAgentsAfterFirstSave() {
        when(mlDecisionService.predictMedical(any())).thenReturn(samplePrediction("MEDICAL"));
        stubSaveAssignsId();

        orchestrator.createAndAnalyzeMedical(sampleMedical(true));

        InOrder order = inOrder(decisionRepository, domainAgentConsultationService);
        order.verify(decisionRepository).save(any(Decision.class));
        order.verify(domainAgentConsultationService).consultAgents(
                any(Decision.class), eq(DecisionDomain.MEDICAL), any(), eq(agent));
    }

    @Test
    void agentFailure_doesNotPreventAudit_whenConsultationReturnsFalse() {
        when(mlDecisionService.predictEducation(any())).thenReturn(samplePrediction("EDUCATION"));
        stubSaveAssignsId();
        when(domainAgentConsultationService.consultAgents(any(), any(), any(), any())).thenReturn(false);

        DecisionResponse response = orchestrator.createAndAnalyzeEducation(sampleEducation(true));

        assertNotNull(response.getDecisionId());
        verify(auditLogService).record(any(UUID.class), eq(agent), eq("ANALYSE"), any(), any(), any(), any());
    }

    private void stubSaveAssignsId() {
        when(decisionRepository.save(any(Decision.class))).thenAnswer(inv -> {
            Decision d = inv.getArgument(0);
            if (d.getDecisionId() == null) {
                d.setDecisionId(UUID.randomUUID());
            }
            return d;
        });
    }

    private static DomainPredictionResponse samplePrediction(String domain) {
        DomainPredictionResponse p = new DomainPredictionResponse();
        p.setDomain(domain);
        p.setPrediction("RISQUE_MOYEN");
        p.setConfidence(0.7);
        p.setRiskLevel("MOYEN");
        p.setModelType("LogisticRegression");
        p.setModelVersion(domain.toLowerCase() + "-model-v1");
        p.setDatasetVersion(domain.toLowerCase() + "-dataset-v1");
        p.setAnalysisId(UUID.randomUUID().toString());
        p.setRecommendation("Test");
        p.setExplanationMethod("feature_importance");
        DomainPredictionResponse.Factor f = new DomainPredictionResponse.Factor();
        f.setFeature("feature_a");
        f.setImportance(0.4);
        f.setImpact("POSITIVE");
        p.setFactors(List.of(f));
        return p;
    }

    private static CreateEducationDecisionRequest sampleEducation(boolean agents) {
        CreateEducationDecisionRequest r = new CreateEducationDecisionRequest();
        r.setRegion("Fes-Meknes");
        r.setTypeEtablissement("UNIVERSITE_PUBLIQUE");
        r.setFiliere("SCIENCES");
        r.setNiveauEtude("L2");
        r.setMoyenneSemestre1(9.5);
        r.setMoyenneSemestre2(8.8);
        r.setTauxAbsence(22.0);
        r.setModulesNonValides(3);
        r.setParticipation("FAIBLE");
        r.setBourse("OUI");
        r.setDistanceLogementKm(18.0);
        r.setAccesInternet("OUI");
        r.setActiviteProfessionnelle("NON");
        r.setHistoriqueRedoublement("NON");
        r.setSituationAcademique("DIFFICULTE");
        r.setDescription("Test EDUCATION");
        r.setIncludeAgents(agents);
        return r;
    }

    private static CreateCreditDecisionRequest sampleCredit(boolean agents) {
        CreateCreditDecisionRequest r = new CreateCreditDecisionRequest();
        r.setSecteurActivite("COMMERCE");
        r.setRegion("Casablanca-Settat");
        r.setAgeDemandeur(35);
        r.setStatutProfessionnel("SALARIE_CDI");
        r.setRevenuMensuelMad(8500.0);
        r.setChargesMensuellesMad(3200.0);
        r.setMontantDemandeMad(40000.0);
        r.setDureeCreditMois(36);
        r.setAncienneteProfessionnelleAnnees(5);
        r.setCreditsExistants(1);
        r.setIncidentsPaiement24Mois(0);
        r.setRatioEndettement(0.35);
        r.setTypeGarantie("AUCUNE");
        r.setTypeCredit("CONSOMMATION");
        r.setIncludeAgents(agents);
        return r;
    }

    private static CreateMedicalDecisionRequest sampleMedical(boolean agents) {
        CreateMedicalDecisionRequest r = new CreateMedicalDecisionRequest();
        r.setRegion("Rabat-Sale-Kenitra");
        r.setAge(48);
        r.setSexe("FEMME");
        r.setImc(29.5);
        r.setNiveauActivitePhysique("SEDENTAIRE");
        r.setAntecedentsFamiliauxDiabete("OUI");
        r.setHypertension("OUI");
        r.setGlycemie(1.25);
        r.setPolyurie("NON");
        r.setPolydipsie("NON");
        r.setPertePoidsSoudaine("NON");
        r.setFaiblesse("OUI");
        r.setObesite("OUI");
        r.setSuiviMedical("OUI");
        r.setIncludeAgents(agents);
        return r;
    }
}
