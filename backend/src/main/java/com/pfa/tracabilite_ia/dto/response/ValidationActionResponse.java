package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationActionResponse {
    private UUID validationActionId;
    private UUID decisionId;
    private UUID validateurId;
    private String validateurNom;
    private TypeActionEnum typeAction;
    private StatutDecisionEnum statutAvant;
    private StatutDecisionEnum statutApres;
    private String decisionHumaine;
    private String commentaire;
    private LocalDateTime timestamp;
}
