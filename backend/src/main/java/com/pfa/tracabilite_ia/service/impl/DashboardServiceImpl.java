package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.response.ComparaisonAgentResponse;
import com.pfa.tracabilite_ia.dto.response.DashboardResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentDefinition;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRegistryService;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.ComparaisonService;
import com.pfa.tracabilite_ia.service.DashboardService;
import com.pfa.tracabilite_ia.service.HashChainService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_LIMIT = 10;

    private final DecisionRepository decisionRepository;
    private final ComparaisonService comparaisonService;
    private final HashChainService hashChainService;
    private final OpenRouterAgentRegistryService openRouterAgentRegistryService;
    private final AuthService authService;

    public DashboardServiceImpl(DecisionRepository decisionRepository,
                                ComparaisonService comparaisonService,
                                HashChainService hashChainService,
                                OpenRouterAgentRegistryService openRouterAgentRegistryService,
                                AuthService authService) {
        this.decisionRepository = decisionRepository;
        this.comparaisonService = comparaisonService;
        this.hashChainService = hashChainService;
        this.openRouterAgentRegistryService = openRouterAgentRegistryService;
        this.authService = authService;
    }

    private Predicate<Decision> getRoleBasedFilter() {
        Utilisateur user = authService.getCurrentUser();
        if (user == null || user.getRole() == null) {
            return d -> false; // Restrict all if not auth
        }

        RoleEnum role = user.getRole();
        
        if (role == RoleEnum.ADMINISTRATEUR || role == RoleEnum.AUDITEUR) {
            return d -> true; // See everything
        }

        return d -> {
            DecisionDomain domain = d.getDomaine() != null ? d.getDomaine() : DecisionDomain.CREDIT;
            
            boolean domainMatch = switch (role) {
                case AGENT_CREDIT, RESPONSABLE_CREDIT -> domain == DecisionDomain.CREDIT;
                case AGENT_SANTE, PROFESSIONNEL_SANTE -> domain == DecisionDomain.MEDICAL;
                case AGENT_PEDAGOGIQUE, RESPONSABLE_PEDAGOGIQUE -> domain == DecisionDomain.EDUCATION;
                case VALIDATEUR -> true;
                default -> false;
            };

            if (!domainMatch) {
                return false;
            }

            // Agents only see their own decisions
            if (role == RoleEnum.AGENT_CREDIT || role == RoleEnum.AGENT_SANTE || role == RoleEnum.AGENT_PEDAGOGIQUE) {
                return user.getEmail() != null && user.getEmail().equalsIgnoreCase(d.getCreatedBy());
            }

            return true;
        };
    }

    private List<Decision> getScopedDecisions() {
        return decisionRepository.findAll().stream()
                .filter(getRoleBasedFilter())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenirStatistiques() {
        List<Decision> scopedDecisions = getScopedDecisions();
        long total = scopedDecisions.size();
        
        long approuvees = scopedDecisions.stream().filter(d -> d.getStatutValidation() == StatutDecisionEnum.APPROUVEE).count();
        long modifiees = scopedDecisions.stream().filter(d -> d.getStatutValidation() == StatutDecisionEnum.MODIFIEE).count();
        long rejetees = scopedDecisions.stream().filter(d -> d.getStatutValidation() == StatutDecisionEnum.REJETEE).count();
        long enAttente = scopedDecisions.stream().filter(d -> d.getStatutValidation() == StatutDecisionEnum.EN_ATTENTE).count();
        long brouillon = scopedDecisions.stream().filter(d -> d.getStatutValidation() == StatutDecisionEnum.BROUILLON).count();

        double tauxValidation = total == 0 ? 0.0d
                : Math.round((approuvees * 1000.0d) / total) / 10.0d;

        List<OpenRouterAgentDefinition> agents = openRouterAgentRegistryService.configuredAgents();
        String agentsLabel = agents.stream()
                .map(OpenRouterAgentDefinition::displayName)
                .collect(Collectors.joining(" · "));

        List<ComparaisonAgentResponse> agentPerformance = comparaisonService.classerAgentsOpenRouter();
        
        // Ensure recent is scoped too
        List<DashboardResponse.RecentDecisionSummary> recent = decisionRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(0, 1000)) // Fetch larger chunk
                .stream()
                .filter(getRoleBasedFilter())
                .limit(RECENT_LIMIT)
                .map(this::toRecentSummary)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalDecisions(total)
                .approuvees(approuvees)
                .modifiees(modifiees)
                .rejetees(rejetees)
                .enAttente(enAttente)
                .brouillon(brouillon)
                .tauxValidation(tauxValidation)
                .agentsActifs(agents.size())
                .agentsLabel(agentsLabel)
                .hashChainIntact(hashChainService.verifierIntegrite())
                .generatedAt(LocalDateTime.now())
                .recentDecisions(recent)
                .agentPerformance(agentPerformance)
                .build();
    }

    private DashboardResponse.RecentDecisionSummary toRecentSummary(Decision decision) {
        String agentLabel = decision.getModelName();
        if (decision.getReponsesAgents() != null && !decision.getReponsesAgents().isEmpty()) {
            agentLabel = "ML + OpenRouter";
        } else if (decision.getSystemeIa() != null) {
            agentLabel = decision.getSystemeIa().getNom();
        }

        return DashboardResponse.RecentDecisionSummary.builder()
                .decisionId(decision.getDecisionId())
                .prompt(decision.getPrompt())
                .modelName(decision.getModelName())
                .agentLabel(agentLabel)
                .statutValidation(decision.getStatutValidation())
                .timestamp(decision.getTimestamp())
                .riskLevel(decision.getRiskLevel())
                .confidenceScore(decision.getConfidenceScore())
                .reference(decision.getDossierReference())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TimelineData> getTimelineStats() {
        List<Decision> scopedDecisions = getScopedDecisions();
        java.util.Map<Integer, Long> createdByMonth = scopedDecisions.stream()
            .collect(Collectors.groupingBy(d -> {
                LocalDateTime ts = d.getSubmittedAt() != null ? d.getSubmittedAt() : d.getTimestamp();
                return ts != null ? ts.getMonthValue() : LocalDateTime.now().getMonthValue();
            }, Collectors.counting()));
        
        java.util.Map<Integer, Long> solvedByMonth = scopedDecisions.stream()
            .filter(d -> d.getStatutValidation() == StatutDecisionEnum.APPROUVEE || 
                         d.getStatutValidation() == StatutDecisionEnum.MODIFIEE || 
                         d.getStatutValidation() == StatutDecisionEnum.REJETEE)
            .collect(Collectors.groupingBy(d -> {
                LocalDateTime ts = d.getValidatedAt() != null ? d.getValidatedAt() : (d.getUpdatedAt() != null ? d.getUpdatedAt() : d.getTimestamp());
                return ts != null ? ts.getMonthValue() : LocalDateTime.now().getMonthValue();
            }, Collectors.counting()));
        
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
        List<com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TimelineData> result = new java.util.ArrayList<>();
        
        int currentMonth = LocalDateTime.now().getMonthValue();
        for (int i = 6; i >= 0; i--) {
            int m = currentMonth - i;
            if (m <= 0) m += 12;
            String label = months[m - 1];
            long created = createdByMonth.getOrDefault(m, 0L);
            long solved = solvedByMonth.getOrDefault(m, 0L);
            result.add(new com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TimelineData(label, created, solved));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TypeStats getTypeStats() {
        List<Decision> scopedDecisions = getScopedDecisions();
        java.util.Map<String, Long> counts = scopedDecisions.stream()
            .collect(Collectors.groupingBy(d -> d.getDomaine() != null ? d.getDomaine().name() : "NON_DEFINI", Collectors.counting()));
        return new com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TypeStats(counts);
    }

    @Override
    @Transactional(readOnly = true)
    public com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.DailyStats getDailyStats() {
        List<Decision> scopedDecisions = getScopedDecisions();
        java.util.Map<String, Long> counts = scopedDecisions.stream()
            .collect(Collectors.groupingBy(d -> d.getTimestamp().getDayOfWeek().name(), Collectors.counting()));
        return new com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.DailyStats(counts);
    }

    @Override
    @Transactional(readOnly = true)
    public com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.KpiData getKpiStats() {
        List<Decision> scopedDecisions = getScopedDecisions();
        long newT = scopedDecisions.stream().filter(d -> d.getStatutValidation() == StatutDecisionEnum.EN_ATTENTE || d.getStatutValidation() == StatutDecisionEnum.BROUILLON).count();
        long retT = scopedDecisions.stream().filter(d -> d.getStatutValidation() == StatutDecisionEnum.MODIFIEE || d.getStatutValidation() == StatutDecisionEnum.REJETEE).count();
        
        long activeCount = scopedDecisions.size();
        long validated = scopedDecisions.stream().filter(d -> 
            d.getStatutValidation() == StatutDecisionEnum.APPROUVEE || 
            d.getStatutValidation() == StatutDecisionEnum.MODIFIEE || 
            d.getStatutValidation() == StatutDecisionEnum.REJETEE).count();
        double approvalRate = activeCount == 0 ? 0.0 : (validated * 100.0) / activeCount;
        approvalRate = Math.round(approvalRate * 10.0) / 10.0;

        long highRiskCount = scopedDecisions.stream().filter(d -> "HIGH".equalsIgnoreCase(d.getRiskLevel())).count();
        long mediumRiskCount = scopedDecisions.stream().filter(d -> "MEDIUM".equalsIgnoreCase(d.getRiskLevel())).count();
        long lowRiskCount = scopedDecisions.stream().filter(d -> "LOW".equalsIgnoreCase(d.getRiskLevel())).count();
        long unknownRiskCount = activeCount - highRiskCount - mediumRiskCount - lowRiskCount;

        java.util.Map<String, Long> riskBreakdown = new java.util.HashMap<>();
        riskBreakdown.put("Élevé", highRiskCount);
        riskBreakdown.put("Modéré", mediumRiskCount);
        riskBreakdown.put("Faible", lowRiskCount);
        if (unknownRiskCount > 0) riskBreakdown.put("Non Spécifié", unknownRiskCount);

        java.util.Map<String, Object> domainMetrics = new java.util.HashMap<>();
        Utilisateur user = authService.getCurrentUser();
        RoleEnum role = user != null ? user.getRole() : null;

        if (role == RoleEnum.AGENT_CREDIT || role == RoleEnum.RESPONSABLE_CREDIT || role == RoleEnum.VALIDATEUR) {
            double totalMontant = scopedDecisions.stream()
                .filter(d -> d.getCreditData() != null && d.getCreditData().getMontantDemandeMad() != null)
                .mapToDouble(d -> d.getCreditData().getMontantDemandeMad())
                .sum();
            double avgTaux = scopedDecisions.stream()
                .filter(d -> d.getCreditData() != null && d.getCreditData().getTauxEndettement() != null)
                .mapToDouble(d -> d.getCreditData().getTauxEndettement())
                .average().orElse(0.0);
            domainMetrics.put("totalMontant", totalMontant);
            domainMetrics.put("avgTaux", Math.round(avgTaux * 10.0) / 10.0);
        } else if (role == RoleEnum.AGENT_SANTE || role == RoleEnum.PROFESSIONNEL_SANTE) {
            double avgGlycemie = scopedDecisions.stream()
                .filter(d -> d.getMedicalData() != null && d.getMedicalData().getGlycemieMgDl() != null)
                .mapToDouble(d -> d.getMedicalData().getGlycemieMgDl())
                .average().orElse(0.0);
            double avgImc = scopedDecisions.stream()
                .filter(d -> d.getMedicalData() != null && d.getMedicalData().getImcKgM2() != null)
                .mapToDouble(d -> d.getMedicalData().getImcKgM2())
                .average().orElse(0.0);
            domainMetrics.put("avgGlycemie", Math.round(avgGlycemie * 10.0) / 10.0);
            domainMetrics.put("avgImc", Math.round(avgImc * 10.0) / 10.0);
        } else if (role == RoleEnum.AGENT_PEDAGOGIQUE || role == RoleEnum.RESPONSABLE_PEDAGOGIQUE) {
            double avgMoyenneS1 = scopedDecisions.stream()
                .filter(d -> d.getEducationData() != null && d.getEducationData().getMoyenneS1() != null)
                .mapToDouble(d -> d.getEducationData().getMoyenneS1())
                .average().orElse(0.0);
            double avgMoyenneS2 = scopedDecisions.stream()
                .filter(d -> d.getEducationData() != null && d.getEducationData().getMoyenneS2() != null)
                .mapToDouble(d -> d.getEducationData().getMoyenneS2())
                .average().orElse(0.0);
            long boursierCount = scopedDecisions.stream()
                .filter(d -> d.getEducationData() != null && "OUI".equalsIgnoreCase(d.getEducationData().getBoursier()))
                .count();
            domainMetrics.put("avgMoyenneS1", Math.round(avgMoyenneS1 * 100.0) / 100.0);
            domainMetrics.put("avgMoyenneS2", Math.round(avgMoyenneS2 * 100.0) / 100.0);
            domainMetrics.put("boursierCount", boursierCount);
        } else if (role == RoleEnum.AUDITEUR) {
            long totalTraceable = scopedDecisions.stream()
                .filter(d -> d.getCurrentHash() != null)
                .count();
            long verifiedTraces = scopedDecisions.stream()
                .filter(d -> d.getCurrentHash() != null && d.getCurrentHash().equals(d.calculerHash()))
                .count();
            double complianceRatio = totalTraceable == 0 ? 100.0 : (verifiedTraces * 100.0) / totalTraceable;
            domainMetrics.put("complianceRatio", Math.round(complianceRatio * 10.0) / 10.0);
            domainMetrics.put("verifiedTraces", verifiedTraces);
            domainMetrics.put("totalTraceable", totalTraceable);
        }

        return com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.KpiData.builder()
            .approvalRate(approvalRate)
            .highRiskCount(highRiskCount)
            .newTickets(newT)
            .returnedTickets(retT)
            .domainMetrics(domainMetrics)
            .riskBreakdown(riskBreakdown)
            .build();
    }
}
