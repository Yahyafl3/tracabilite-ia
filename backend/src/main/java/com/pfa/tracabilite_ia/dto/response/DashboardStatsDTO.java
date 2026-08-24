package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    @Valid
    @NotNull(message = "KPI values cannot be null")
    private KPIValues kpiValues;
    
    @NotNull(message = "Timeline data cannot be null")
    private List<TimelineDataPoint> timelineData;
    
    @Valid
    @NotNull(message = "Status distribution cannot be null")
    private StatusDistribution statusDistribution;
    
    @Valid
    private DomainDistribution domainDistribution;
    
    private List<CreatorStats> topCreators;
    
    @NotNull(message = "Recent decisions cannot be null")
    private List<RecentDecisionDTO> recentDecisions;
    
    private List<ValidationActionDTO> recentValidations;
}
