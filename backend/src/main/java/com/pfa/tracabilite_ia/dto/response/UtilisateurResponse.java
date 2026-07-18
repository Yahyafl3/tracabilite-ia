package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UtilisateurResponse {
    private UUID id;
    private String nom;
    private String email;
    private RoleEnum role;
    private LocalDateTime dateCreation;
}
