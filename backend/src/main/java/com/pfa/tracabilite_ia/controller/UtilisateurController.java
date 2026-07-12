package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur creer(@Valid @RequestBody CreerUtilisateurRequest request) {
        return utilisateurService.creer(request);
    }

    @GetMapping
    public List<Utilisateur> lister() {
        return utilisateurService.lister();
    }
}
