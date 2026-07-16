package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.response.ComparaisonAgentResponse;
import com.pfa.tracabilite_ia.service.ComparaisonService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comparaison")
@PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR', 'AUDITOR')")
public class ComparaisonController {

    private final ComparaisonService comparaisonService;

    public ComparaisonController(ComparaisonService comparaisonService) {
        this.comparaisonService = comparaisonService;
    }

    @GetMapping
    public List<ComparaisonAgentResponse> classerAgents() {
        return comparaisonService.classerAgentsOpenRouter();
    }
}