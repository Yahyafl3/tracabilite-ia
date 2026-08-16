package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse obtenirStatistiques();

    java.util.List<com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TimelineData> getTimelineStats();
    
    com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.TypeStats getTypeStats();
    
    com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.DailyStats getDailyStats();
    
    com.pfa.tracabilite_ia.dto.response.DashboardChartResponse.KpiData getKpiStats();
}
