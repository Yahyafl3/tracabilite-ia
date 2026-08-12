package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.*;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.repository.AuditLogRepository;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating dashboard statistics with role-based data isolation.
 * All statistics come from real database queries - no mock data or hardcoded values.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleDashboardService {
    
    private final DecisionRepository decisionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogRepository auditLogRepository;
    private final DashboardIsolationService isolationService;
    
    /**
     * Calculates dashboard statistics for the authenticated user.
     * Data is automatically scoped based on the user's role.
     *
     * @param user The authenticated user
     * @return Dashboard statistics DTO with all KPIs, charts, and tables
     */
    @Transactional(readOnly = true, timeout = 2)
    public DashboardStatsDTO calculateDashboardStats(Utilisateur user) {
        log.debug("Calculating dashboard stats for user: {} with role: {}", user.getEmail(), user.getRole());
        
        // Build scoped query based on user role
        Specification<Decision> scope = isolationService.buildDashboardScope(user);
        
        // Fetch authorized decisions
        List<Decision> decisions = decisionRepository.findAll(scope);
        
        log.debug("Found {} decisions for user {}", decisions.size(), user.getEmail());
        
        // Calculate role-specific statistics
        return buildDashboardDTO(decisions, user);
    }
    
    /**
     * Builds the complete dashboard DTO from filtered decisions.
     */
    private DashboardStatsDTO buildDashboardDTO(List<Decision> decisions, Utilisateur user) {
        RoleEnum role = user.getRole();
        
        // Determine time range based on role
        int timeRangeDays = isManagerOrAuditor(role) ? 30 : 7;
        
        return DashboardStatsDTO.builder()
                .kpiValues(calculateKPIs(decisions, user))
                .timelineData(calculateTimeline(decisions, timeRangeDays))
                .statusDistribution(calculateStatusDistribution(decisions))
                .domainDistribution(calculateDomainDistribution(decisions, user))
                .topCreators(calculateTopCreators(decisions, user))
                .recentDecisions(getRecentDecisions(decisions))
                .recentValidations(getRecentValidations(decisions, user))
                .build();
    }
    
    /**
     * Calculates all KPI values based on user role.
     */
    private KPIValues calculateKPIs(List<Decision> decisions, Utilisateur user) {
        RoleEnum role = user.getRole();
        long total = decisions.size();
        
        KPIValues.KPIValuesBuilder builder = KPIValues.builder()
                .totalDecisions(total);
        
        // Role-specific KPIs
        switch (role) {
            case ADMINISTRATEUR -> {
                builder.pendingValidations(countByStatus(decisions, StatutDecisionEnum.EN_ATTENTE_VALIDATION));
                builder.todaysDecisions(countDecisionsToday(decisions));
                builder.activeUsers(countActiveUsers());
            }
            case AGENT_CREDIT, AGENT_SANTE, AGENT_PEDAGOGIQUE -> {
                long validated = countByStatus(decisions, StatutDecisionEnum.VALIDEE);
                long rejected = countByStatus(decisions, StatutDecisionEnum.REJETEE);
                builder.pendingValidations(countByStatus(decisions, StatutDecisionEnum.EN_ATTENTE_VALIDATION));
                builder.validatedDecisions(validated);
                builder.acceptanceRate(calculateRate(validated, total));
            }
            case VALIDATEUR -> {
                long validated = countValidatedByUser(decisions, user.getEmail());
                long rejected = countRejectedByUser(decisions, user.getEmail());
                builder.pendingValidations(countByStatus(decisions, StatutDecisionEnum.EN_ATTENTE_VALIDATION));
                builder.validatedDecisions(validated);
                builder.rejectedDecisions(rejected);
                builder.processedDecisions(validated + rejected);
            }
            case RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE -> {
                long validated = countValidatedThisMonth(decisions);
                builder.pendingValidations(countByStatus(decisions, StatutDecisionEnum.EN_ATTENTE_VALIDATION));
                builder.validatedDecisions(validated);
                builder.validationRate(calculateRate(validated, total));
            }
            case AUDITEUR -> {
                long validated = countByStatus(decisions, StatutDecisionEnum.VALIDEE);
                long rejected = countByStatus(decisions, StatutDecisionEnum.REJETEE);
                builder.validatedDecisions(validated);
                builder.rejectedDecisions(rejected);
                builder.complianceRate(calculateRate(validated, validated + rejected));
            }
        }
        
        return builder.build();
    }
    
    /**
     * Calculates timeline data for the specified number of days.
     */
    private List<TimelineDataPoint> calculateTimeline(List<Decision> decisions, int days) {
        LocalDate today = LocalDate.now();
        List<TimelineDataPoint> timeline = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            long count = decisions.stream()
                    .filter(d -> d.getTimestamp() != null)
                    .filter(d -> !d.getTimestamp().isBefore(startOfDay) && !d.getTimestamp().isAfter(endOfDay))
                    .count();
            
            long validationCount = decisions.stream()
                    .filter(d -> d.getValidatedAt() != null)
                    .filter(d -> !d.getValidatedAt().isBefore(startOfDay) && !d.getValidatedAt().isAfter(endOfDay))
                    .count();
            
            timeline.add(TimelineDataPoint.builder()
                    .date(date)
                    .decisionCount(count)
                    .validationCount(validationCount)
                    .build());
        }
        
        return timeline;
    }
    
    /**
     * Calculates status distribution.
     */
    private StatusDistribution calculateStatusDistribution(List<Decision> decisions) {
        return StatusDistribution.builder()
                .enAttenteValidation(countByStatus(decisions, StatutDecisionEnum.EN_ATTENTE_VALIDATION))
                .validee(countByStatus(decisions, StatutDecisionEnum.VALIDEE))
                .rejetee(countByStatus(decisions, StatutDecisionEnum.REJETEE))
                .build();
    }
    
    /**
     * Calculates domain distribution (only for multi-domain roles).
     */
    private DomainDistribution calculateDomainDistribution(List<Decision> decisions, Utilisateur user) {
        RoleEnum role = user.getRole();
        
        // Only multi-domain roles get domain distribution
        if (role != RoleEnum.ADMINISTRATEUR && role != RoleEnum.AUDITEUR) {
            return null;
        }
        
        return DomainDistribution.builder()
                .creditCount(countByDomain(decisions, DecisionDomain.CREDIT))
                .medicalCount(countByDomain(decisions, DecisionDomain.MEDICAL))
                .educationCount(countByDomain(decisions, DecisionDomain.EDUCATION))
                .build();
    }
    
    /**
     * Calculates top creators (for applicable roles).
     */
    private List<CreatorStats> calculateTopCreators(List<Decision> decisions, Utilisateur user) {
        RoleEnum role = user.getRole();
        
        // Only managers and admins see top creators
        if (!isManagerOrAdmin(role)) {
            return Collections.emptyList();
        }
        
        Map<String, Long> creatorCounts = decisions.stream()
                .filter(d -> d.getCreatedBy() != null && !d.getCreatedBy().isBlank())
                .collect(Collectors.groupingBy(Decision::getCreatedBy, Collectors.counting()));
        
        return creatorCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    String email = entry.getKey();
                    Utilisateur creator = utilisateurRepository.findByEmail(email).orElse(null);
                    String name = creator != null ? creator.getNom() : email;
                    
                    return CreatorStats.builder()
                            .creatorEmail(email)
                            .creatorName(name)
                            .decisionCount(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the 10 most recent decisions.
     */
    private List<RecentDecisionDTO> getRecentDecisions(List<Decision> decisions) {
        return decisions.stream()
                .filter(d -> d.getTimestamp() != null)
                .sorted(Comparator.comparing(Decision::getTimestamp).reversed())
                .limit(10)
                .map(this::mapToRecentDecisionDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the 10 most recent validation actions (for applicable roles).
     */
    private List<ValidationActionDTO> getRecentValidations(List<Decision> decisions, Utilisateur user) {
        RoleEnum role = user.getRole();
        
        // Only validators, managers, admins, and auditors see validations
        if (!isValidatorManagerAdminOrAuditor(role)) {
            return Collections.emptyList();
        }
        
        return decisions.stream()
                .filter(d -> d.getValidatedAt() != null)
                .filter(d -> d.getStatutValidation() == StatutDecisionEnum.VALIDEE 
                          || d.getStatutValidation() == StatutDecisionEnum.REJETEE)
                .sorted(Comparator.comparing(Decision::getValidatedAt).reversed())
                .limit(10)
                .map(this::mapToValidationActionDTO)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private long countByStatus(List<Decision> decisions, StatutDecisionEnum status) {
        return decisions.stream()
                .filter(d -> d.getStatutValidation() == status)
                .count();
    }
    
    private long countByDomain(List<Decision> decisions, DecisionDomain domain) {
        return decisions.stream()
                .filter(d -> d.getDomaine() == domain)
                .count();
    }
    
    private long countDecisionsToday(List<Decision> decisions) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return decisions.stream()
                .filter(d -> d.getTimestamp() != null && !d.getTimestamp().isBefore(startOfToday))
                .count();
    }
    
    private long countValidatedThisMonth(List<Decision> decisions) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        
        return decisions.stream()
                .filter(d -> d.getValidatedAt() != null)
                .filter(d -> !d.getValidatedAt().isBefore(startOfMonth) && !d.getValidatedAt().isAfter(endOfMonth))
                .filter(d -> d.getStatutValidation() == StatutDecisionEnum.VALIDEE)
                .count();
    }
    
    private long countValidatedByUser(List<Decision> decisions, String email) {
        return decisions.stream()
                .filter(d -> d.getStatutValidation() == StatutDecisionEnum.VALIDEE)
                .filter(d -> email.equalsIgnoreCase(d.getValidatorEmail()))
                .count();
    }
    
    private long countRejectedByUser(List<Decision> decisions, String email) {
        return decisions.stream()
                .filter(d -> d.getStatutValidation() == StatutDecisionEnum.REJETEE)
                .filter(d -> email.equalsIgnoreCase(d.getValidatorEmail()))
                .count();
    }
    
    private long countActiveUsers() {
        return utilisateurRepository.count();
    }
    
    private double calculateRate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return (double) numerator / denominator;
    }
    
    private boolean isManagerOrAuditor(RoleEnum role) {
        return role == RoleEnum.RESPONSABLE_CREDIT 
            || role == RoleEnum.PROFESSIONNEL_SANTE 
            || role == RoleEnum.RESPONSABLE_PEDAGOGIQUE
            || role == RoleEnum.AUDITEUR;
    }
    
    private boolean isManagerOrAdmin(RoleEnum role) {
        return role == RoleEnum.ADMINISTRATEUR
            || role == RoleEnum.RESPONSABLE_CREDIT 
            || role == RoleEnum.PROFESSIONNEL_SANTE 
            || role == RoleEnum.RESPONSABLE_PEDAGOGIQUE;
    }
    
    private boolean isValidatorManagerAdminOrAuditor(RoleEnum role) {
        return role == RoleEnum.VALIDATEUR
            || role == RoleEnum.ADMINISTRATEUR
            || role == RoleEnum.AUDITEUR
            || role == RoleEnum.RESPONSABLE_CREDIT 
            || role == RoleEnum.PROFESSIONNEL_SANTE 
            || role == RoleEnum.RESPONSABLE_PEDAGOGIQUE;
    }
    
    private RecentDecisionDTO mapToRecentDecisionDTO(Decision decision) {
        Utilisateur creator = utilisateurRepository.findByEmail(decision.getCreatedBy()).orElse(null);
        String creatorName = creator != null ? creator.getNom() : decision.getCreatedBy();
        
        return RecentDecisionDTO.builder()
                .decisionId(decision.getDecisionId())
                .reference(decision.getDossierReference())
                .domaine(decision.getDomaine())
                .statutValidation(decision.getStatutValidation())
                .createdAt(decision.getTimestamp())
                .createdBy(decision.getCreatedBy())
                .creatorName(creatorName)
                .build();
    }
    
    private ValidationActionDTO mapToValidationActionDTO(Decision decision) {
        Utilisateur validator = utilisateurRepository.findByEmail(decision.getValidatorEmail()).orElse(null);
        String validatorName = validator != null ? validator.getNom() : decision.getValidatorEmail();
        
        return ValidationActionDTO.builder()
                .decisionId(decision.getDecisionId())
                .decisionReference(decision.getDossierReference())
                .domaine(decision.getDomaine())
                .statutValidation(decision.getStatutValidation())
                .validatedAt(decision.getValidatedAt())
                .validatedBy(decision.getValidatorEmail())
                .validatorName(validatorName)
                .commentaire(decision.getJustificationHumaine())
                .build();
    }
}
