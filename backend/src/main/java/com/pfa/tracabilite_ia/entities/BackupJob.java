package com.pfa.tracabilite_ia.entities;

import com.pfa.tracabilite_ia.enumeration.BackupJobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "backup_job")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private UUID createdByUserId;

    @Column(length = 255)
    private String createdByEmail;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String packSha256;

    @Column(nullable = false)
    private int decisionCount;

    @Column(nullable = false)
    private int userCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BackupJobStatus status;

    private LocalDateTime lastVerifiedAt;

    private LocalDateTime lastRestoredAt;

    private Integer restoreUsersCreated;

    private Integer restoreUsersSkipped;

    private Integer restoreDecisionsCreated;

    private Integer restoreDecisionsSkipped;
}
