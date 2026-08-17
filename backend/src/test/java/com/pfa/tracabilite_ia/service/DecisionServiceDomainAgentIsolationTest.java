package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests d'isolation des décisions pour les agents de domaine (AGENT_CREDIT, AGENT_SANTE, AGENT_PEDAGOGIQUE).
 * Vérifie que chaque agent ne voit que ses propres décisions dans son domaine.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DecisionServiceDomainAgentIsolationTest {

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private DecisionScopeService decisionScopeService;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Utilisateur agentCredit1;
    private Utilisateur agentCredit2;
    private Utilisateur agentSante;
    private Utilisateur agentPedagogique;
    private Utilisateur administrateur;

    private Decision creditDec1; // créée par agentCredit1
    private Decision creditDec2; // créée par agentCredit2
    private Decision medicalDec; // créée par agentSante
    private Decision educationDec; // créée par agentPedagogique
    private Decision creditDecByAdmin; // créée par ADMINISTRATEUR
    private Decision medicalDecByAdmin; // créée par ADMINISTRATEUR
    private Decision educationDecByAdmin; // créée par ADMINISTRATEUR

    @BeforeEach
    void setUp() {
        // Créer les utilisateurs
        agentCredit1 = createUser("agent.credit1@test.com", RoleEnum.AGENT_CREDIT);
        agentCredit2 = createUser("agent.credit2@test.com", RoleEnum.AGENT_CREDIT);
        agentSante = createUser("agent.sante@test.com", RoleEnum.AGENT_SANTE);
        agentPedagogique = createUser("agent.pedagogique@test.com", RoleEnum.AGENT_PEDAGOGIQUE);
        administrateur = createUser("admin@test.com", RoleEnum.ADMINISTRATEUR);

        // Créer les décisions
        creditDec1 = createDecision("CREDIT par agent1", DecisionDomain.CREDIT, agentCredit1.getEmail());
        creditDec2 = createDecision("CREDIT par agent2", DecisionDomain.CREDIT, agentCredit2.getEmail());
        medicalDec = createDecision("MEDICAL par agentSante", DecisionDomain.MEDICAL, agentSante.getEmail());
        educationDec = createDecision("EDUCATION par agentPedagogique", DecisionDomain.EDUCATION, agentPedagogique.getEmail());
        
        // Créer les décisions par ADMINISTRATEUR
        creditDecByAdmin = createDecision("CREDIT par ADMIN", DecisionDomain.CREDIT, administrateur.getEmail());
        medicalDecByAdmin = createDecision("MEDICAL par ADMIN", DecisionDomain.MEDICAL, administrateur.getEmail());
        educationDecByAdmin = createDecision("EDUCATION par ADMIN", DecisionDomain.EDUCATION, administrateur.getEmail());
    }

    @Test
    void agentCredit_shouldOnlySeeOwnCreditDecisions() {
        // Given
        authenticateAs(agentCredit1);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting("decisionId")
                .contains(creditDec1.getDecisionId())
                .doesNotContain(creditDec2.getDecisionId());
    }

    @Test
    void agentCredit_shouldNotSeeOtherAgentCreditDecisions() {
        // Given
        authenticateAs(agentCredit1);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then
        assertThat(response.getContent())
                .extracting("decisionId")
                .doesNotContain(creditDec2.getDecisionId());
    }

    @Test
    void agentSante_shouldOnlySeeOwnMedicalDecisions() {
        // Given
        authenticateAs(agentSante);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting("decisionId")
                .contains(medicalDec.getDecisionId())
                .doesNotContain(creditDec1.getDecisionId(), creditDec2.getDecisionId(), educationDec.getDecisionId());
    }

    @Test
    void agentPedagogique_shouldOnlySeeOwnEducationDecisions() {
        // Given
        authenticateAs(agentPedagogique);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting("decisionId")
                .contains(educationDec.getDecisionId())
                .doesNotContain(creditDec1.getDecisionId(), creditDec2.getDecisionId(), medicalDec.getDecisionId());
    }

    @Test
    void agentCredit_cannotAccessOtherAgentDecisionById() {
        // Given
        authenticateAs(agentCredit1);

        // When / Then
        assertThatThrownBy(() -> decisionScopeService.loadForRead(creditDec2.getDecisionId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void agentCredit_canAccessOwnDecisionById() {
        // Given
        authenticateAs(agentCredit1);

        // When
        Decision loaded = decisionScopeService.loadForRead(creditDec1.getDecisionId());

        // Then
        assertThat(loaded).isNotNull();
        assertThat(loaded.getDecisionId()).isEqualTo(creditDec1.getDecisionId());
    }

    @Test
    void agentSante_cannotAccessCreditDecisionById() {
        // Given
        authenticateAs(agentSante);

        // When / Then
        assertThatThrownBy(() -> decisionScopeService.loadForRead(creditDec1.getDecisionId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void agentCredit_shouldNotSeeMedicalOrEducationDecisions() {
        // Given
        authenticateAs(agentCredit1);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then
        assertThat(response.getContent())
                .extracting("decisionId")
                .doesNotContain(medicalDec.getDecisionId(), educationDec.getDecisionId());
    }

    @Test
    void agentSante_shouldNotSeeCreditOrEducationDecisions() {
        // Given
        authenticateAs(agentSante);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then
        assertThat(response.getContent())
                .extracting("decisionId")
                .doesNotContain(creditDec1.getDecisionId(), creditDec2.getDecisionId(), educationDec.getDecisionId());
    }

    @Test
    void agentPedagogique_shouldNotSeeCreditOrMedicalDecisions() {
        // Given
        authenticateAs(agentPedagogique);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then
        assertThat(response.getContent())
                .extracting("decisionId")
                .doesNotContain(creditDec1.getDecisionId(), creditDec2.getDecisionId(), medicalDec.getDecisionId());
    }

    // ===== Tests pour les décisions créées par ADMINISTRATEUR =====

    @Test
    void agentCredit_shouldSeeAdministrateurCreditDecisions() {
        // Given
        authenticateAs(agentCredit1);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then - doit voir sa propre décision + celle de l'ADMINISTRATEUR en CREDIT
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting("decisionId")
                .containsExactlyInAnyOrder(creditDec1.getDecisionId(), creditDecByAdmin.getDecisionId());
    }

    @Test
    void agentSante_shouldSeeAdministrateurMedicalDecisions() {
        // Given
        authenticateAs(agentSante);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then - doit voir sa propre décision + celle de l'ADMINISTRATEUR en MEDICAL
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting("decisionId")
                .containsExactlyInAnyOrder(medicalDec.getDecisionId(), medicalDecByAdmin.getDecisionId());
    }

    @Test
    void agentPedagogique_shouldSeeAdministrateurEducationDecisions() {
        // Given
        authenticateAs(agentPedagogique);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then - doit voir sa propre décision + celle de l'ADMINISTRATEUR en EDUCATION
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting("decisionId")
                .containsExactlyInAnyOrder(educationDec.getDecisionId(), educationDecByAdmin.getDecisionId());
    }

    @Test
    void agentCredit_shouldNotSeeAdministrateurMedicalOrEducationDecisions() {
        // Given
        authenticateAs(agentCredit1);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then - ne doit PAS voir les décisions ADMINISTRATEUR des autres domaines
        assertThat(response.getContent())
                .extracting("decisionId")
                .doesNotContain(medicalDecByAdmin.getDecisionId(), educationDecByAdmin.getDecisionId());
    }

    @Test
    void agentSante_shouldNotSeeAdministrateurCreditOrEducationDecisions() {
        // Given
        authenticateAs(agentSante);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then - ne doit PAS voir les décisions ADMINISTRATEUR des autres domaines
        assertThat(response.getContent())
                .extracting("decisionId")
                .doesNotContain(creditDecByAdmin.getDecisionId(), educationDecByAdmin.getDecisionId());
    }

    @Test
    void agentPedagogique_shouldNotSeeAdministrateurCreditOrMedicalDecisions() {
        // Given
        authenticateAs(agentPedagogique);

        // When
        DecisionPageResponse response = decisionService.rechercher(null, null, 0, 50);

        // Then - ne doit PAS voir les décisions ADMINISTRATEUR des autres domaines
        assertThat(response.getContent())
                .extracting("decisionId")
                .doesNotContain(creditDecByAdmin.getDecisionId(), medicalDecByAdmin.getDecisionId());
    }

    @Test
    void agentCredit_canAccessAdministrateurCreditDecisionById() {
        // Given
        authenticateAs(agentCredit1);

        // When
        Decision loaded = decisionScopeService.loadForRead(creditDecByAdmin.getDecisionId());

        // Then
        assertThat(loaded).isNotNull();
        assertThat(loaded.getDecisionId()).isEqualTo(creditDecByAdmin.getDecisionId());
        assertThat(loaded.getCreatedBy()).isEqualTo(administrateur.getEmail());
    }

    @Test
    void agentSante_canAccessAdministrateurMedicalDecisionById() {
        // Given
        authenticateAs(agentSante);

        // When
        Decision loaded = decisionScopeService.loadForRead(medicalDecByAdmin.getDecisionId());

        // Then
        assertThat(loaded).isNotNull();
        assertThat(loaded.getDecisionId()).isEqualTo(medicalDecByAdmin.getDecisionId());
        assertThat(loaded.getCreatedBy()).isEqualTo(administrateur.getEmail());
    }

    @Test
    void agentPedagogique_canAccessAdministrateurEducationDecisionById() {
        // Given
        authenticateAs(agentPedagogique);

        // When
        Decision loaded = decisionScopeService.loadForRead(educationDecByAdmin.getDecisionId());

        // Then
        assertThat(loaded).isNotNull();
        assertThat(loaded.getDecisionId()).isEqualTo(educationDecByAdmin.getDecisionId());
        assertThat(loaded.getCreatedBy()).isEqualTo(administrateur.getEmail());
    }

    @Test
    void agentCredit_cannotAccessAdministrateurMedicalDecisionById() {
        // Given
        authenticateAs(agentCredit1);

        // When / Then
        assertThatThrownBy(() -> decisionScopeService.loadForRead(medicalDecByAdmin.getDecisionId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void agentSante_cannotAccessAdministrateurCreditDecisionById() {
        // Given
        authenticateAs(agentSante);

        // When / Then
        assertThatThrownBy(() -> decisionScopeService.loadForRead(creditDecByAdmin.getDecisionId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    // Helper methods

    private Utilisateur createUser(String email, RoleEnum role) {
        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setRole(role);
        user.setMotDePasseHash("hashed");
        user.setNom("Test User");
        user.setActif(true);
        return utilisateurRepository.save(user);
    }

    private Decision createDecision(String prompt, DecisionDomain domain, String createdBy) {
        Decision decision = new Decision();
        decision.setPrompt(prompt);
        decision.setDomaine(domain);
        decision.setCreatedBy(createdBy);
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        decision.setReponse("Test response");
        decision.setModelName("test-model");
        decision.setModelVersion("1.0");
        decision.setTimestamp(LocalDateTime.now());
        return decisionRepository.save(decision);
    }

    private void authenticateAs(Utilisateur user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId().toString(), null, null)
        );
    }
}
