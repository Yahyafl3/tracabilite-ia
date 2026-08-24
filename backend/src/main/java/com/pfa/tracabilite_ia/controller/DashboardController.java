package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.response.DashboardStatsDTO;
import com.pfa.tracabilite_ia.dto.response.ErrorResponse;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.RoleDashboardService;
import jakarta.persistence.QueryTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for dashboard statistics endpoints.
 * Enforces authentication and provides role-based dashboard data.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final RoleDashboardService roleDashboardService;
    private final AuthService authService;

    /**
     * Get dashboard statistics for the authenticated user.
     * Statistics are automatically scoped based on the user's role.
     *
     * @return Dashboard statistics DTO
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        log.debug("Dashboard stats requested");

        Utilisateur currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            log.warn("Dashboard stats requested but no authenticated user found");
            throw new AuthenticationException("Utilisateur non authentifié") {};
        }

        log.info("Dashboard stats requested by user: {} with role: {}",
                currentUser.getEmail(), currentUser.getRole());

        DashboardStatsDTO stats = roleDashboardService.calculateDashboardStats(currentUser);

        return ResponseEntity.ok(stats);
    }

    /**
     * Exception handler for authentication errors.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error in dashboard: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("AUTHENTICATION_REQUIRED")
                .message("Authentification requise pour accéder au dashboard")
                .path("/api/dashboard/stats")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Exception handler for authorization errors.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String username = "unknown";
        try {
            Utilisateur user = authService.getCurrentUser();
            if (user != null) {
                username = user.getEmail();
            }
        } catch (Exception e) {
            // Ignore if we can't get current user
        }

        log.warn("Access denied for user: {} - {}", username, ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("ACCESS_DENIED")
                .message("Accès refusé au dashboard")
                .path("/api/dashboard/stats")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Exception handler for query timeout errors.
     */
    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleQueryTimeoutException(QueryTimeoutException ex) {
        log.error("Dashboard query timeout: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("QUERY_TIMEOUT")
                .message("Le calcul des statistiques a pris trop de temps. Veuillez réessayer dans quelques instants.")
                .path("/api/dashboard/stats")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Generic exception handler for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error in dashboard controller", ex);

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("Une erreur inattendue est survenue lors du chargement du dashboard")
                .path("/api/dashboard/stats")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @GetMapping("/stats/timeline")
    public java.util.List<com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TimelineData> getTimelineStats() {
        return dashboardService.getTimelineStats();
    }

    @GetMapping("/stats/by-type")
    public com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TypeStats getTypeStats() {
        return dashboardService.getTypeStats();
    }

    @GetMapping("/stats/daily")
    public com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.DailyStats getDailyStats() {
        return dashboardService.getDailyStats();
    }

    @GetMapping("/stats/kpi")
    public com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.KpiData getKpiStats() {
        return dashboardService.getKpiStats();
    }
}
