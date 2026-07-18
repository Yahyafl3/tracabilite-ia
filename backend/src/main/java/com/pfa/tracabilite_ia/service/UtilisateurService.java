package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.dto.request.ModifierUtilisateurRequest;
import com.pfa.tracabilite_ia.dto.response.UtilisateurResponse;

import java.util.List;
import java.util.UUID;

public interface UtilisateurService {
    UtilisateurResponse creer(CreerUtilisateurRequest request);

    List<UtilisateurResponse> lister();

    UtilisateurResponse obtenir(UUID id);

    UtilisateurResponse modifier(UUID id, ModifierUtilisateurRequest request);

    void supprimer(UUID id);
}
