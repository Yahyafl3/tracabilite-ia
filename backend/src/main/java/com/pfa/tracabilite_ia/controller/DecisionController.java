package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.DecisionRequest;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.service.DecisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decisions")
@PreAuthorize("isAuthenticated()")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Decision creer(@Valid @RequestBody DecisionRequest request) {
        return decisionService.creer(request);
    }

    @GetMapping
    public List<Decision> lister() {
        return decisionService.lister();
    }

    @GetMapping("/{id}")
    public Decision obtenir(@PathVariable UUID id) {
        return decisionService.obtenir(id);
    }

    @PutMapping("/{id}")
    public Decision mettreAJour(@PathVariable UUID id,
                                @Valid @RequestBody DecisionRequest request) {
        return decisionService.mettreAJour(id, request);
    }
}
