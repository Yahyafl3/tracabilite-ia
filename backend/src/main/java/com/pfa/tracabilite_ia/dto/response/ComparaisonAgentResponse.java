package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparaisonAgentResponse {

    private Integer rang;
    private UUID systemeIaId;
    private String nom;
    private String fournisseur;
    private String modele;
    private String versionModele;
    private long totalDecisions;
    private long approuvees;
    private long modifiees;
    private long rejetees;
    private long enAttente;
    private double scorePourcentage;
}