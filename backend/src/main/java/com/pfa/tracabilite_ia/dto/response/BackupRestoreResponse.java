package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupRestoreResponse {

    private UUID id;
    private int usersCreated;
    private int usersSkipped;
    private int decisionsCreated;
    private int decisionsSkipped;
    private String packSha256;
}
