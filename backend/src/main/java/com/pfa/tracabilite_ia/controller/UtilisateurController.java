package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.dto.request.ModifierUtilisateurRequest;
import com.pfa.tracabilite_ia.dto.response.UtilisateurResponse;
import com.pfa.tracabilite_ia.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponse creer(@Valid @RequestBody CreerUtilisateurRequest request) {
        return utilisateurService.creer(request);
    }

    @GetMapping
    public List<UtilisateurResponse> lister() {
        return utilisateurService.lister();
    }

    @GetMapping("/{id}")
    public UtilisateurResponse obtenir(@PathVariable UUID id) {
        return utilisateurService.obtenir(id);
    }

    @PutMapping("/{id}")
    public UtilisateurResponse modifier(@PathVariable UUID id,
                                      @Valid @RequestBody ModifierUtilisateurRequest request) {
        return utilisateurService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        utilisateurService.supprimer(id);
    }
}
