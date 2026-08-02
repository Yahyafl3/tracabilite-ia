package com.pfa.tracabilite_ia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID decisionId;

    private UUID userId;

    @Column(length = 64)
    private String userRole;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(length = 64)
    private String oldStatus;

    @Column(length = 64)
    private String newStatus;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 64)
    private String correlationId;
}
