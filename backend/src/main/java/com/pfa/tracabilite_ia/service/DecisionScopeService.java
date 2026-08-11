package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Charge une décision avec ses données strictement scoped par decisionId et vérifie les droits d'accès.
 */
@Service
public class DecisionScopeService {

    private final DecisionRepository decisionRepository;
    private final ReponseAgentIARepository reponseAgentIARepository;
    private final AuthService authService;
    private final UtilisateurRepository utilisateurRepository;

    public DecisionScopeService(DecisionRepository decisionRepository,
                                ReponseAgentIARepository reponseAgentIARepository,
                                AuthService authService,
                                UtilisateurRepository utilisateurRepository) {
        this.decisionRepository = decisionRepository;
        this.reponseAgentIARepository = reponseAgentIARepository;
        this.authService = authService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public Decision loadForRead(UUID decisionId) {
        Decision decision = decisionRepository.findByIdWithFactors(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));

        checkReadAccess(decision, authService.getCurrentUser());

        attachScopedAgentResponses(decision);
        return decision;
    }

    @Transactional(readOnly = true)
    public Decision loadForValidation(UUID decisionId) {
        return loadForRead(decisionId);
    }

    public void attachScopedAgentResponses(Decision decision) {
        List<ReponseAgentIA> scopedAgents = reponseAgentIARepository
                .findByDecisionDecisionIdOrderByAgentKeyAsc(decision.getDecisionId());
        decision.getReponsesAgents().clear();
        for (ReponseAgentIA agent : scopedAgents) {
            agent.setDecision(decision);
            decision.getReponsesAgents().add(agent);
        }
    }

    private void checkReadAccess(Decision decision, Utilisateur user) {
        if (user == null || user.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié ou sans rôle");
        }

        RoleEnum role = user.getRole();

        // Admins and Auditors can view everything
        if (role == RoleEnum.ADMINISTRATEUR || role == RoleEnum.AUDITEUR) {
            return;
        }

        // Users can view their own decisions
        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(decision.getCreatedBy())) {
            return;
        }

        // Domain agents can view decisions created by ADMINISTRATEUR in their domain
        if (decision.getCreatedBy() != null && !decision.getCreatedBy().isEmpty()) {
            // Check if creator is ADMINISTRATEUR
            try {
                Utilisateur creator = utilisateurRepository.findByEmail(decision.getCreatedBy()).orElse(null);
                if (creator != null && creator.getRole() == RoleEnum.ADMINISTRATEUR) {
                    DecisionDomain domain = decision.getDomaine() != null ? decision.getDomaine() : DecisionDomain.CREDIT;
                    boolean isDomainAgent = switch (domain) {
                        case CREDIT -> role == RoleEnum.AGENT_CREDIT;
                        case MEDICAL -> role == RoleEnum.AGENT_SANTE;
                        case EDUCATION -> role == RoleEnum.AGENT_PEDAGOGIQUE;
                    };
                    if (isDomainAgent) {
                        return;
                    }
                }
            } catch (Exception e) {
                // If we can't find the creator, continue with normal access check
            }
        }

        DecisionDomain domain = decision.getDomaine() != null ? decision.getDomaine() : DecisionDomain.CREDIT;

        boolean allowed = switch (domain) {
            case CREDIT -> role == RoleEnum.RESPONSABLE_CREDIT || role == RoleEnum.VALIDATEUR;
            case MEDICAL -> role == RoleEnum.PROFESSIONNEL_SANTE || role == RoleEnum.VALIDATEUR;
            case EDUCATION -> role == RoleEnum.RESPONSABLE_PEDAGOGIQUE || role == RoleEnum.VALIDATEUR;
        };

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Accès refusé: Votre rôle " + role + " ne permet pas de consulter une décision du domaine " + domain);
        }
    }
}
