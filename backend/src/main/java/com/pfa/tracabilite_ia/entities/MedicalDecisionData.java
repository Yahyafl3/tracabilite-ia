package com.pfa.tracabilite_ia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "medical_decision_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDecisionData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false, unique = true)
    private Decision decision;

    private Integer age;
    private Integer grossesses;
    private Double glycemieMgDl;
    private Double pressionArterielleMmhg;
    private Double epaisseurPliCutaneMm;
    private Double insulineMicroUMl;
    private Double imcKgM2;
}
