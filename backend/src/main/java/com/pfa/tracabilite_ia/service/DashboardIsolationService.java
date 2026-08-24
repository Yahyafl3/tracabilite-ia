package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.List;

/**
 * Service responsible for enforcing data isolation rules for dashboard statistics.
 * Reuses the same isolation logic as DecisionScopeService but in Specification format
 * for efficient database queries.
 */
@Service
@RequiredArgsConstructor
public class DashboardIsolationService {
    
    private final UtilisateurRepository utilisateurRepository;
    
    /**
     * Builds a JPA Specification that enforces role-based data isolation.
     * 
     * @param user The authenticated user
     * @return A Specification that filters decisions according to the user's role
     */
    public Specification<Decision> buildDashboardScope(Utilisateur user) {
        if (user == null || user.getRole() == null) {
            // No user or role - return empty result set
            return (root, query, cb) -> cb.disjunction();
        }
        
        RoleEnum role = user.getRole();
        
        return switch (role) {
            case ADMINISTRATEUR, AUDITEUR -> allDomainsSpec();
            case AGENT_CREDIT -> agentScopeSpec(user, DecisionDomain.CREDIT);
            case AGENT_SANTE -> agentScopeSpec(user, DecisionDomain.MEDICAL);
            case AGENT_PEDAGOGIQUE -> agentScopeSpec(user, DecisionDomain.EDUCATION);
            case RESPONSABLE_CREDIT -> domainOnlySpec(DecisionDomain.CREDIT);
            case PROFESSIONNEL_SANTE -> domainOnlySpec(DecisionDomain.MEDICAL);
            case RESPONSABLE_PEDAGOGIQUE -> domainOnlySpec(DecisionDomain.EDUCATION);
            case UTILISATEUR -> throw new IllegalStateException("UTILISATEUR role does not have dashboard access");
            default -> throw new IllegalStateException("Unsupported role: " + role);
        };
    }
    
    /**
     * Agent scope: own decisions + ADMINISTRATEUR decisions in the same domain.
     * This preserves the existing agent isolation rule.
     */
    private Specification<Decision> agentScopeSpec(Utilisateur user, DecisionDomain domain) {
        return (root, query, cb) -> {
            // Domain must match
            Predicate domainMatch = cb.equal(root.get("domaine"), domain);
            
            // Own decisions
            Predicate ownDecisions = cb.equal(root.get("createdBy"), user.getEmail());
            
            // ADMINISTRATEUR decisions in same domain
            List<String> adminEmails = utilisateurRepository.findEmailsByRole(RoleEnum.ADMINISTRATEUR);
            Predicate adminDecisions = adminEmails.isEmpty() 
                ? cb.disjunction() // No admins exist
                : root.get("createdBy").in(adminEmails);
            
            // Combine: (own OR admin) AND domain
            return cb.and(domainMatch, cb.or(ownDecisions, adminDecisions));
        };
    }
    
    /**
     * Domain-only scope: all decisions in the specified domain.
     * Used for validators and managers.
     */
    private Specification<Decision> domainOnlySpec(DecisionDomain domain) {
        return (root, query, cb) -> cb.equal(root.get("domaine"), domain);
    }
    
    /**
     * All domains scope: no filtering by domain or creator.
     * Used for ADMINISTRATEUR and AUDITEUR roles.
     */
    private Specification<Decision> allDomainsSpec() {
        return (root, query, cb) -> cb.conjunction(); // No filter = all records
    }
}
