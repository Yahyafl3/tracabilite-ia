package com.pfa.tracabilite_ia.mapper;

import com.pfa.tracabilite_ia.dto.response.UtilisateurResponse;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UtilisateurMapper {

    public UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .dateCreation(utilisateur.getDateCreation())
                .build();
    }

    public List<UtilisateurResponse> toResponseList(List<Utilisateur> utilisateurs) {
        return utilisateurs.stream().map(this::toResponse).toList();
    }
}
