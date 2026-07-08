package com.pfa.tracabilite_ia.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditAnalysisRequest {
    private Integer revenuMensuel;
    private Integer dettesActuelles;
    private Integer age;
    private Integer ancienneteEmploi;
    private Integer montantDemande;
}
