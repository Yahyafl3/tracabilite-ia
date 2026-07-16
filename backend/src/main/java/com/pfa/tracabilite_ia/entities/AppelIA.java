package com.pfa.tracabilite_ia.entities;

import com.pfa.tracabilite_ia.enumeration.StatutAppelIAEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appel_ia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppelIA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID appelIaId;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String model;

    private String modelVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userPrompt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    private Long durationMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAppelIAEnum statut;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private String correlationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
}
