package com.pfa.tracabilite_ia.entities;

import com.pfa.tracabilite_ia.enumeration.DecisionSourceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "decision_source")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sourceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionSourceType sourceType;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(length = 512)
    private String documentReference;

    @Column(length = 64)
    private String contentHash;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    private UUID createdById;

    @Column(length = 255)
    private String createdByEmail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
