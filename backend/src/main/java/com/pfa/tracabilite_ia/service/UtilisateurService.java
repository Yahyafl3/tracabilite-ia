package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.entities.Utilisateur;

import java.util.List;

public interface UtilisateurService {
    Utilisateur creer(CreerUtilisateurRequest request);
    List<Utilisateur> lister();
}
