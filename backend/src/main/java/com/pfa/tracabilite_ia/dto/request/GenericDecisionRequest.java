package com.pfa.tracabilite_ia.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO générique pour tous les domaines de décision
 * Supporte: credit, medical, insurance, hr, legal, education, general
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericDecisionRequest {
    /**
     * Domaine de décision: credit, medical, insurance, hr, legal, education, general
     */
    private String domain;
    
    /**
     * Features dynamiques selon le domaine
     * Exemples:
     * - credit: {"revenuMensuel": 15000, "dettesActuelles": 2000, "age": 35, "ancienneteEmploi": 5}
     * - medical: {"urgence": 0.8, "risque": 0.3, "disponibilite": 0.9, "priorite": 0.7}
     * - insurance: {"risqueClient": 0.4, "historique": 0.8, "montantCouverture": 0.6, "age": 0.7}
     */
    private Map<String, Object> features;
    
    /**
     * Description de la demande
     */
    private String description;
    
    /**
     * Métadonnées additionnelles (optionnel)
     */
    private Map<String, Object> metadata;
}
