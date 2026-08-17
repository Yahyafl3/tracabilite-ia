package com.pfa.tracabilite_ia.enumeration;

/**
 * Rôles métier des comptes internes.
 * <p>
 * {@link #AGENT_CREDIT}, {@link #AGENT_SANTE}, {@link #AGENT_PEDAGOGIQUE} sont les agents
 * créateurs de décisions par domaine. Ils ne valident pas.
 * {@link #RESPONSABLE_CREDIT}, {@link #PROFESSIONNEL_SANTE},
 * {@link #RESPONSABLE_PEDAGOGIQUE} sont les validateurs spécialisés par domaine.
 * {@link #ADMINISTRATEUR} gère les comptes et a une visibilité globale.
 * <p>
 * {@link #UTILISATEUR} et {@link #VALIDATEUR} sont retirés du modèle de permissions :
 * les comptes concernés sont migrés au démarrage vers {@link #AGENT_CREDIT} et
 * {@link #RESPONSABLE_CREDIT}. Les constantes ne subsistent que le temps que les JWT
 * déjà émis expirent, afin qu'ils restent désérialisables.
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
    /** @deprecated migré vers {@link #AGENT_CREDIT}. Ne confère plus aucune permission. */
    @Deprecated(forRemoval = true)
    UTILISATEUR,
    /** @deprecated migré vers {@link #RESPONSABLE_CREDIT}. Ne confère plus aucune permission. */
    @Deprecated(forRemoval = true)
    VALIDATEUR
}
