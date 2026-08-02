package com.pfa.tracabilite_ia.enumeration;

/**
 * Statuts du cycle de vie d'une décision.
 * Les valeurs historiques (EN_ATTENTE, APPROUVEE, MODIFIEE, REJETEE) sont
 * conservées pour compatibilité avec les décisions existantes.
 */
public enum StatutDecisionEnum {
    BROUILLON,
    EN_ANALYSE,
    ANALYSEE,
    EN_ATTENTE_VALIDATION,
    /** @deprecated Compat — préférer EN_ATTENTE_VALIDATION */
    EN_ATTENTE,
    VALIDEE,
    /** @deprecated Compat — préférer VALIDEE */
    APPROUVEE,
    MODIFIEE,
    A_REVOIR,
    REJETEE,
    ARCHIVEE
}
