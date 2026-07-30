package com.pfa.tracabilite_ia.enumeration;

/**
 * Rôles métier des comptes internes.
 * <p>
 * {@link #UTILISATEUR} = Agent métier (interne). Mapped to Spring {@code ROLE_USER}.
 * Les rôles {@link #RESPONSABLE_CREDIT}, {@link #PROFESSIONNEL_SANTE},
 * {@link #RESPONSABLE_PEDAGOGIQUE} sont des validateurs spécialisés par domaine.
 * {@link #VALIDATEUR} reste supporté pour compatibilité (validation générique).
 * {@link #ADMINISTRATEUR} administre mais n'est pas validateur métier par défaut.
 */
public enum RoleEnum {
    ADMINISTRATEUR,
    VALIDATEUR,
    AUDITEUR,
    /** Agent métier (interne). Spring: ROLE_USER. */
    UTILISATEUR,
    RESPONSABLE_CREDIT,
    PROFESSIONNEL_SANTE,
    RESPONSABLE_PEDAGOGIQUE
}
