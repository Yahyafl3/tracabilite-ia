package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.response.OpenRouterModelStatusResponse;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRegistryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/openrouter")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOpenRouterController {

    private final OpenRouterAgentRegistryService agentRegistryService;

    public AdminOpenRouterController(OpenRouterAgentRegistryService agentRegistryService) {
        this.agentRegistryService = agentRegistryService;
    }

    @GetMapping("/models/status")
    public List<OpenRouterModelStatusResponse> modelStatuses() {
        return agentRegistryService.modelStatuses();
    }
}
