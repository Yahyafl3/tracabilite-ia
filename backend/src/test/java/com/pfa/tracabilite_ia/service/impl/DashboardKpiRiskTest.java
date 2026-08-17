package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.response.DashboardChartResponse;
import com.pfa.tracabilite_ia.entities.CreditDecisionData;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRegistryService;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.ComparaisonService;
import com.pfa.tracabilite_ia.service.HashChainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Le service ML multidomaine écrit riskLevel="ELEVE" alors que le parcours crédit
 * historique écrit "HIGH". Les deux doivent alimenter les mêmes KPI de risque.
 */
class DashboardKpiRiskTest {

    private DecisionRepository decisionRepository;
    private AuthService authService;
    private UtilisateurRepository utilisateurRepository;
    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        decisionRepository = mock(DecisionRepository.class);
        authService = mock(AuthService.class);
        utilisateurRepository = mock(UtilisateurRepository.class);
        service = new DashboardServiceImpl(
                decisionRepository,
                mock(ComparaisonService.class),
                mock(HashChainService.class),
                mock(OpenRouterAgentRegistryService.class),
                authService,
                utilisateurRepository
        );

        Utilisateur admin = new Utilisateur();
        admin.setEmail("admin@tracabilite.ia");
        admin.setRole(RoleEnum.ADMINISTRATEUR);
        when(authService.getCurrentUser()).thenReturn(admin);
    }

    private Decision decision(String riskLevel) {
        Decision decision = new Decision();
        decision.setDomaine(DecisionDomain.CREDIT);
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        decision.setRiskLevel(riskLevel);
        decision.setConfidenceScore(0.6816d);
        decision.setTimestamp(LocalDateTime.now());
        return decision;
    }

    @Test
    void pendingFrenchLabelledDecisionCountsInBothHighRiskKpis() {
        when(decisionRepository.findAll()).thenReturn(List.of(decision("ELEVE")));

        DashboardChartResponse.KpiData kpi = service.getKpiStats();

        assertThat(kpi.getHighRiskCount()).isEqualTo(1);
        assertThat(kpi.getRiskBreakdown()).containsEntry("Élevé", 1L);

        List<Double> risque = kpi.getSparklines().get("risque");
        assertThat(risque).hasSize(7);
        assertThat(risque.get(risque.size() - 1)).isEqualTo(100.0d);
    }

    @Test
    void englishLabelledDecisionStillCounts() {
        when(decisionRepository.findAll()).thenReturn(List.of(decision("HIGH")));

        DashboardChartResponse.KpiData kpi = service.getKpiStats();

        assertThat(kpi.getHighRiskCount()).isEqualTo(1);
        assertThat(kpi.getSparklines().get("risque")).last().isEqualTo(100.0d);
    }

    @Test
    void mixedRiskLevelsAreBucketedConsistently() {
        when(decisionRepository.findAll()).thenReturn(List.of(
                decision("ELEVE"),
                decision("MODERE"),
                decision("MEDIUM"),
                decision("FAIBLE")
        ));

        DashboardChartResponse.KpiData kpi = service.getKpiStats();

        assertThat(kpi.getHighRiskCount()).isEqualTo(1);
        assertThat(kpi.getRiskBreakdown())
                .containsEntry("Élevé", 1L)
                .containsEntry("Moyen", 2L)
                .containsEntry("Faible", 1L);
        assertThat(kpi.getSparklines().get("risque")).last().isEqualTo(25.0d);
    }

    /**
     * Le taux d'endettement est stocké en ratio 0–1 alors que la carte KPI est
     * libellée en pourcentage : 0,30 et 0,50 doivent donner 40 %, pas 0,4 %.
     */
    @Test
    void averageDebtRatioIsExposedAsPercentage() {
        Utilisateur responsable = new Utilisateur();
        responsable.setEmail("credit@tracabilite.ia");
        responsable.setRole(RoleEnum.RESPONSABLE_CREDIT);
        when(authService.getCurrentUser()).thenReturn(responsable);
        when(decisionRepository.findAll()).thenReturn(List.of(
                creditDecision(0.30d, 40000d),
                creditDecision(0.50d, 60000d)
        ));

        DashboardChartResponse.KpiData kpi = service.getKpiStats();

        assertThat(kpi.getDomainMetrics()).containsEntry("avgTaux", 40.0d);
        assertThat(kpi.getDomainMetrics()).containsEntry("totalMontant", 100000.0d);
    }

    /**
     * DecisionScopeService laisse un agent de domaine ouvrir les dossiers créés par un
     * administrateur ; ses indicateurs doivent refléter le même périmètre.
     */
    @Test
    void domainAgentCountsDecisionsOpenedByAnAdministrator() {
        Utilisateur admin = new Utilisateur();
        admin.setEmail("admin@tracabilite.ia");
        admin.setRole(RoleEnum.ADMINISTRATEUR);

        Utilisateur agent = new Utilisateur();
        agent.setEmail("agent.sante@tracabilite.ia");
        agent.setRole(RoleEnum.AGENT_SANTE);
        when(authService.getCurrentUser()).thenReturn(agent);
        when(utilisateurRepository.findByRole(RoleEnum.ADMINISTRATEUR)).thenReturn(List.of(admin));

        Decision createdByAdmin = decision("FAIBLE");
        createdByAdmin.setDomaine(DecisionDomain.MEDICAL);
        createdByAdmin.setCreatedBy("admin@tracabilite.ia");

        Decision createdByAgent = decision("ELEVE");
        createdByAgent.setDomaine(DecisionDomain.MEDICAL);
        createdByAgent.setCreatedBy("agent.sante@tracabilite.ia");

        Decision otherDomain = decision("ELEVE");
        otherDomain.setCreatedBy("admin@tracabilite.ia");

        when(decisionRepository.findAll())
                .thenReturn(List.of(createdByAdmin, createdByAgent, otherDomain));

        DashboardChartResponse.KpiData kpi = service.getKpiStats();

        // Les deux dossiers médicaux comptent, le dossier crédit reste hors périmètre.
        assertThat(kpi.getRiskBreakdown())
                .containsEntry("Faible", 1L)
                .containsEntry("Élevé", 1L);
        assertThat(kpi.getHighRiskCount()).isEqualTo(1);
    }

    @Test
    void reportsHumanAiAlignmentAndIntegrityCounts() {
        Decision agreed = decision("FAIBLE");
        agreed.setStatutValidation(StatutDecisionEnum.VALIDEE);
        agreed.setAccordAvecIa(Boolean.TRUE);
        agreed.setCurrentHash(agreed.calculerHash());

        Decision diverged = decision("ELEVE");
        diverged.setStatutValidation(StatutDecisionEnum.VALIDEE);
        diverged.setAccordAvecIa(Boolean.FALSE);
        diverged.setCurrentHash(diverged.calculerHash());

        Decision pending = decision("MOYEN");
        pending.setCurrentHash("empreinte-qui-ne-se-recalcule-pas");

        when(decisionRepository.findAll()).thenReturn(List.of(agreed, diverged, pending));

        DashboardChartResponse.KpiData kpi = service.getKpiStats();

        assertThat(kpi.getAiAgreement()).isEqualTo(1);
        assertThat(kpi.getAiDisagreement()).isEqualTo(1);
        assertThat(kpi.getAiNotArbitrated()).isEqualTo(1);
        assertThat(kpi.getPendingValidation()).isEqualTo(1);
        assertThat(kpi.getIntegrityTotal()).isEqualTo(3);
        assertThat(kpi.getIntegrityVerified()).isEqualTo(2);
    }

    private Decision creditDecision(double tauxEndettement, double montantDemande) {
        Decision decision = decision("FAIBLE");
        CreditDecisionData data = new CreditDecisionData();
        data.setDecision(decision);
        data.setTauxEndettement(tauxEndettement);
        data.setMontantDemandeMad(montantDemande);
        decision.setCreditData(data);
        return decision;
    }
}
