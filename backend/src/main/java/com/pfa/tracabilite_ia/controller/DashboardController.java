package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.response.DashboardResponse;
import com.pfa.tracabilite_ia.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse obtenirStatistiques() {
        return dashboardService.obtenirStatistiques();
    }
}
