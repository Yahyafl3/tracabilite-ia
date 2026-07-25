package com.pfa.tracabilite_ia.enumeration;

/**
 * Rôles métier des comptes internes.
 * <p>
 * {@link #UTILISATEUR} = Agent de crédit (employé interne). Mapped to Spring {@code ROLE_USER}.
 * Le client / demandeur de crédit n'a pas de compte et n'est pas un rôle.
 */
public enum RoleEnum {
    ADMINISTRATEUR,
    VALIDATEUR,
    AUDITEUR,
    /** Agent de crédit / agent métier (interne). Spring: ROLE_USER. */
    UTILISATEUR,
}
