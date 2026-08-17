package com.pfa.tracabilite_ia.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupPack {

    public static final int SCHEMA_VERSION = 1;

    @Builder.Default
    private int version = SCHEMA_VERSION;

    private LocalDateTime createdAt;

    private String createdByEmail;

    /** SHA-256 of the JSON file bytes (filled after serialization). */
    private String packSha256;

    @Builder.Default
    private List<BackupUserSnapshot> users = new ArrayList<>();

    @Builder.Default
    private List<BackupDecisionSnapshot> decisions = new ArrayList<>();
}
