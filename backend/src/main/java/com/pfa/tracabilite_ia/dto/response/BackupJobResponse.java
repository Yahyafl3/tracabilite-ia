package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.BackupJobStatus;
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
public class BackupJobResponse {

    private UUID id;
    private LocalDateTime createdAt;
    private String createdByEmail;
    private String filename;
    private long sizeBytes;
    private String packSha256;
    private int decisionCount;
    private int userCount;
    private BackupJobStatus status;
    private LocalDateTime lastVerifiedAt;
    private LocalDateTime lastRestoredAt;
    private Integer restoreUsersCreated;
    private Integer restoreUsersSkipped;
    private Integer restoreDecisionsCreated;
    private Integer restoreDecisionsSkipped;
    private boolean filePresent;
}
