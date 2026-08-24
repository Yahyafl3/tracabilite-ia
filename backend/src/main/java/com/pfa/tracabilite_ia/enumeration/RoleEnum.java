package com.pfa.tracabilite_ia.enumeration;

/**
 * Rôles métier des comptes internes.
 * <p>
 * {@link #AGENT_CREDIT}, {@link #AGENT_SANTE}, {@link #AGENT_PEDAGOGIQUE} sont les agents
 * créateurs de décisions par domaine. Ils ne valident pas.
 * {@link #RESPONSABLE_CREDIT}, {@link #PROFESSIONNEL_SANTE},
 * {@link #RESPONSABLE_PEDAGOGIQUE} sont les validateurs spécialisés par domaine.
 * {@link #UTILISATEUR} est un rôle LEGACY conservé pour compatibilité ;
 * il n'est plus proposé à la création de nouveaux comptes.
 * {@link #ADMINISTRATEUR} gère les comptes et a une visibilité globale.
 */
public enum RoleEnum {
    ADMINISTRATEUR,
    AUDITEUR,
    /** Agent créateur — décisions CREDIT uniquement, lecture propres dossiers. */
    AGENT_CREDIT,
    /** Agent créateur — décisions MEDICAL uniquement, lecture propres dossiers. */
    AGENT_SANTE,
    /** Agent créateur — décisions EDUCATION uniquement, lecture propres dossiers. */
    AGENT_PEDAGOGIQUE,
    RESPONSABLE_CREDIT,
    PROFESSIONNEL_SANTE,
    RESPONSABLE_PEDAGOGIQUE,
    /** Legacy — compatibilité. Ne plus proposer à la création. */
    UTILISATEUR
}
