package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.DashboardStatsDTO;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RoleDashboardService with real database.
 * Tests verify that dashboard statistics respect role-based data isolation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleDashboardServiceTest {

    @Autowired
    private RoleDashboardService dashboardService;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Utilisateur admin;
    private Utilisateur agentCredit;
    private Utilisateur agentSante;
    private Utilisateur agentPedagogique;
    private Utilisateur responsableCredit;
    private Utilisateur professionnelSante;
    private Utilisateur responsablePedagogique;
    private Utilisateur auditeur;

    @BeforeEach
    void setUp() {
        // Clean up
        decisionRepository.deleteAll();
        utilisateurRepository.deleteAll();

        // Create users
        admin = createUser("admin@test.com", "Admin", RoleEnum.ADMINISTRATEUR);
        agentCredit = createUser("agent.credit@test.com", "Agent Credit", RoleEnum.AGENT_CREDIT);
        agentSante = createUser("agent.sante@test.com", "Agent Sante", RoleEnum.AGENT_SANTE);
        agentPedagogique = createUser("agent.pedagogique@test.com", "Agent Pedagogique", RoleEnum.AGENT_PEDAGOGIQUE);
        responsableCredit = createUser("responsable.credit@test.com", "Responsable Credit", RoleEnum.RESPONSABLE_CREDIT);
        professionnelSante = createUser("professionnel.sante@test.com", "Professionnel Sante", RoleEnum.PROFESSIONNEL_SANTE);
        responsablePedagogique = createUser("responsable.pedagogique@test.com", "Responsable Pedagogique", RoleEnum.RESPONSABLE_PEDAGOGIQUE);
        auditeur = createUser("auditeur@test.com", "Auditeur", RoleEnum.AUDITEUR);

        // Create test data
        // CREDIT decisions
        createDecision(DecisionDomain.CREDIT, admin.getEmail(), StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        createDecision(DecisionDomain.CREDIT, admin.getEmail(), StatutDecisionEnum.VALIDEE);
        createDecision(DecisionDomain.CREDIT, agentCredit.getEmail(), StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        createDecision(DecisionDomain.CREDIT, agentCredit.getEmail(), StatutDecisionEnum.VALIDEE);
        createDecision(DecisionDomain.CREDIT, "other.agent.credit@test.com", StatutDecisionEnum.EN_ATTENTE_VALIDATION);

        // MEDICAL decisions
        createDecision(DecisionDomain.MEDICAL, admin.getEmail(), StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        createDecision(DecisionDomain.MEDICAL, admin.getEmail(), StatutDecisionEnum.VALIDEE);
        createDecision(DecisionDomain.MEDICAL, agentSante.getEmail(), StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        createDecision(DecisionDomain.MEDICAL, agentSante.getEmail(), StatutDecisionEnum.REJETEE);
        createDecision(DecisionDomain.MEDICAL, "other.agent.sante@test.com", StatutDecisionEnum.VALIDEE);

        // EDUCATION decisions
        createDecision(DecisionDomain.EDUCATION, admin.getEmail(), StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        createDecision(DecisionDomain.EDUCATION, agentPedagogique.getEmail(), StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        createDecision(DecisionDomain.EDUCATION, agentPedagogique.getEmail(), StatutDecisionEnum.VALIDEE);
        createDecision(DecisionDomain.EDUCATION, "other.agent.pedagogique@test.com", StatutDecisionEnum.REJETEE);
    }

    @Test
    void administrateur_seesAllDomains() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(admin);

        assertThat(stats).isNotNull();
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(14);

        // Should see all domains
        assertThat(stats.getDomainDistribution()).isNotNull();
        assertThat(stats.getDomainDistribution().getCreditCount()).isEqualTo(5);
        assertThat(stats.getDomainDistribution().getMedicalCount()).isEqualTo(5);
        assertThat(stats.getDomainDistribution().getEducationCount()).isEqualTo(4);
    }

    @Test
    void agentCredit_seesOnlyCreditDomain() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentCredit);

        assertThat(stats).isNotNull();

        // Should see own CREDIT + ADMINISTRATEUR CREDIT = 2 + 2 = 4
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(4);

        // Should NOT see domain distribution (single domain role)
        assertThat(stats.getDomainDistribution()).isNull();

        // Verify it's only CREDIT decisions
        assertThat(stats.getRecentDecisions()).isNotEmpty();
        assertThat(stats.getRecentDecisions())
                .allMatch(d -> d.getDomaine() == DecisionDomain.CREDIT);
    }

    @Test
    void agentCredit_doesNotSeeMedicalOrEducation() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentCredit);

        // Total should be 4 (not 14)
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(4);

        // Verify status distribution only counts CREDIT decisions
        long totalStatusCount = stats.getStatusDistribution().getEnAttenteValidation()
                + stats.getStatusDistribution().getValidee()
                + stats.getStatusDistribution().getRejetee();

        assertThat(totalStatusCount).isEqualTo(4);
    }

    @Test
    void agentCredit_doesNotSeeOtherAgentCreditDecisions() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentCredit);

        // Should NOT include the decision from "other.agent.credit@test.com"
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(4); // Not 5
    }

    @Test
    void agentSante_seesOnlyMedicalDomain() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentSante);

        assertThat(stats).isNotNull();

        // Should see own MEDICAL + ADMINISTRATEUR MEDICAL = 2 + 2 = 4
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(4);

        // Should NOT see domain distribution (single domain role)
        assertThat(stats.getDomainDistribution()).isNull();

        // Verify it's only MEDICAL decisions
        assertThat(stats.getRecentDecisions()).isNotEmpty();
        assertThat(stats.getRecentDecisions())
                .allMatch(d -> d.getDomaine() == DecisionDomain.MEDICAL);
    }

    @Test
    void agentSante_doesNotSeeCreditOrEducation() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentSante);

        // Total should be 4 (not 14)
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(4);
    }

    @Test
    void agentSante_doesNotSeeOtherAgentSanteDecisions() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentSante);

        // Should NOT include the decision from "other.agent.sante@test.com"
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(4); // Not 5
    }

    @Test
    void agentPedagogique_seesOnlyEducationDomain() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentPedagogique);

        assertThat(stats).isNotNull();

        // Should see own EDUCATION + ADMINISTRATEUR EDUCATION = 2 + 1 = 3
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(3);

        // Should NOT see domain distribution (single domain role)
        assertThat(stats.getDomainDistribution()).isNull();

        // Verify it's only EDUCATION decisions
        assertThat(stats.getRecentDecisions()).isNotEmpty();
        assertThat(stats.getRecentDecisions())
                .allMatch(d -> d.getDomaine() == DecisionDomain.EDUCATION);
    }

    @Test
    void agentPedagogique_doesNotSeeCreditOrMedical() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentPedagogique);

        // Total should be 3 (not 14)
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(3);
    }

    @Test
    void agentPedagogique_doesNotSeeOtherAgentPedagogiqueDecisions() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(agentPedagogique);

        // Should NOT include the decision from "other.agent.pedagogique@test.com"
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(3); // Not 4
    }



    @Test
    void responsableCredit_seesOnlyCreditDomain() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(responsableCredit);

        assertThat(stats).isNotNull();

        // Should see all CREDIT decisions = 5
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(5);

        // Should NOT see domain distribution (single domain role)
        assertThat(stats.getDomainDistribution()).isNull();

        // Verify it's only CREDIT decisions
        if (!stats.getRecentDecisions().isEmpty()) {
            assertThat(stats.getRecentDecisions())
                    .allMatch(d -> d.getDomaine() == DecisionDomain.CREDIT);
        }
    }

    @Test
    void professionnelSante_seesOnlyMedicalDomain() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(professionnelSante);

        assertThat(stats).isNotNull();

        // Should see all MEDICAL decisions = 5
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(5);

        // Should NOT see domain distribution (single domain role)
        assertThat(stats.getDomainDistribution()).isNull();

        // Verify it's only MEDICAL decisions
        if (!stats.getRecentDecisions().isEmpty()) {
            assertThat(stats.getRecentDecisions())
                    .allMatch(d -> d.getDomaine() == DecisionDomain.MEDICAL);
        }
    }

    @Test
    void responsablePedagogique_seesOnlyEducationDomain() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(responsablePedagogique);

        assertThat(stats).isNotNull();

        // Should see all EDUCATION decisions = 4
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(4);

        // Should NOT see domain distribution (single domain role)
        assertThat(stats.getDomainDistribution()).isNull();

        // Verify it's only EDUCATION decisions
        if (!stats.getRecentDecisions().isEmpty()) {
            assertThat(stats.getRecentDecisions())
                    .allMatch(d -> d.getDomaine() == DecisionDomain.EDUCATION);
        }
    }

    @Test
    void auditeur_seesAllDomainsReadOnly() {
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats(auditeur);

        assertThat(stats).isNotNull();
        assertThat(stats.getKpiValues().getTotalDecisions()).isEqualTo(14);

        // Should see all domains
        assertThat(stats.getDomainDistribution()).isNotNull();
        assertThat(stats.getDomainDistribution().getCreditCount()).isEqualTo(5);
        assertThat(stats.getDomainDistribution().getMedicalCount()).isEqualTo(5);
        assertThat(stats.getDomainDistribution().getEducationCount()).isEqualTo(4);
    }

    @Test
    void statusDistribution_respectsScope() {
        // Agent Credit should only see CREDIT status counts
        DashboardStatsDTO creditStats = dashboardService.calculateDashboardStats(agentCredit);

        assertThat(creditStats.getStatusDistribution()).isNotNull();
        assertThat(creditStats.getStatusDistribution().getEnAttenteValidation()).isEqualTo(2);
        assertThat(creditStats.getStatusDistribution().getValidee()).isEqualTo(2);
        assertThat(creditStats.getStatusDistribution().getRejetee()).isEqualTo(0);

        // Admin should see all statuses across all domains
        DashboardStatsDTO adminStats = dashboardService.calculateDashboardStats(admin);
        assertThat(adminStats.getStatusDistribution().getEnAttenteValidation()).isEqualTo(7);
        assertThat(adminStats.getStatusDistribution().getValidee()).isEqualTo(5);
        assertThat(adminStats.getStatusDistribution().getRejetee()).isEqualTo(2);
    }

    @Test
    void timeline_respectsScope() {
        DashboardStatsDTO creditStats = dashboardService.calculateDashboardStats(agentCredit);

        assertThat(creditStats.getTimelineData()).isNotEmpty();

        // Timeline should only count decisions within scope
        long totalInTimeline = creditStats.getTimelineData().stream()
                .mapToLong(t -> t.getDecisionCount())
                .sum();

        assertThat(totalInTimeline).isEqualTo(4); // Only CREDIT decisions
    }

    @Test
    void recentDecisions_respectsScope() {
        DashboardStatsDTO creditStats = dashboardService.calculateDashboardStats(agentCredit);

        assertThat(creditStats.getRecentDecisions()).isNotEmpty();
        assertThat(creditStats.getRecentDecisions()).hasSizeLessThanOrEqualTo(10);

        // All should be CREDIT domain
        assertThat(creditStats.getRecentDecisions())
                .allMatch(d -> d.getDomaine() == DecisionDomain.CREDIT);

        // Should include admin's CREDIT decisions
        assertThat(creditStats.getRecentDecisions())
                .anyMatch(d -> d.getCreatedBy().equals(admin.getEmail()));

        // Should include own decisions
        assertThat(creditStats.getRecentDecisions())
                .anyMatch(d -> d.getCreatedBy().equals(agentCredit.getEmail()));
    }

    @Test
    void topCreators_onlyForManagersAndAdmin() {
        // Admin should see top creators
        DashboardStatsDTO adminStats = dashboardService.calculateDashboardStats(admin);
        assertThat(adminStats.getTopCreators()).isNotEmpty();

        // Manager should see top creators
        DashboardStatsDTO managerStats = dashboardService.calculateDashboardStats(responsableCredit);
        assertThat(managerStats.getTopCreators()).isNotEmpty();

        // Agent should NOT see top creators
        DashboardStatsDTO agentStats = dashboardService.calculateDashboardStats(agentCredit);
        assertThat(agentStats.getTopCreators()).isEmpty();
    }

    // Helper methods

    private Utilisateur createUser(String email, String nom, RoleEnum role) {
        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setNom(nom);
        user.setRole(role);
        user.setMotDePasseHash("hash");
        user.setActif(true);
        return utilisateurRepository.save(user);
    }

    private Decision createDecision(DecisionDomain domain, String createdBy, StatutDecisionEnum statut) {
        Decision decision = new Decision();
        decision.setDomaine(domain);
        decision.setCreatedBy(createdBy);
        decision.setStatutValidation(statut);
        decision.setPrompt("Test prompt");
        decision.setModelName("test-model");
        decision.setReponse("Test response");
        decision.setTimestamp(LocalDateTime.now());
        return decisionRepository.save(decision);
    }
}
