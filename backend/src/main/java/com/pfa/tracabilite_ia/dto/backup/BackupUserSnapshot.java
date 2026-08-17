package com.pfa.tracabilite_ia.dto.backup;

import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** User snapshot without password hash. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupUserSnapshot {

    private UUID id;
    private String nom;
    private String email;
    private RoleEnum role;
    private boolean actif;
    private LocalDateTime dateCreation;
}
