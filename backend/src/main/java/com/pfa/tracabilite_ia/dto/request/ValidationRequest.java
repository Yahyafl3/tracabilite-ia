package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ValidationRequest {

    @Size(max = 2000, message = "Le commentaire ne peut pas dépasser 2000 caractères")
    private String commentaire;

    /** Décision humaine finale : APPROUVER ou REJETER (requis pour MODIFIER). */
    private String decisionHumaine;
}
