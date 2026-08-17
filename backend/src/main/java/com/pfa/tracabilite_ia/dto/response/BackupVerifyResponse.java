package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.BackupJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupVerifyResponse {

    private UUID id;
    private boolean valid;
    private String expectedSha256;
    private String actualSha256;
    private BackupJobStatus status;
}
