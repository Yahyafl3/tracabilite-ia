package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.response.OpenRouterKeyStatusResponse;
import com.pfa.tracabilite_ia.dto.response.OpenRouterModelStatusResponse;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRegistryService;
import com.pfa.tracabilite_ia.openrouter.OpenRouterKeyStatusService;
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
    private final OpenRouterKeyStatusService keyStatusService;

    public AdminOpenRouterController(OpenRouterAgentRegistryService agentRegistryService,
                                     OpenRouterKeyStatusService keyStatusService) {
        this.agentRegistryService = agentRegistryService;
        this.keyStatusService = keyStatusService;
    }

    @GetMapping("/models/status")
    public List<OpenRouterModelStatusResponse> modelStatuses() {
        return agentRegistryService.modelStatuses();
    }

    @GetMapping("/key-status")
    public OpenRouterKeyStatusResponse keyStatus() {
        return keyStatusService.fetchStatus();
    }
}
