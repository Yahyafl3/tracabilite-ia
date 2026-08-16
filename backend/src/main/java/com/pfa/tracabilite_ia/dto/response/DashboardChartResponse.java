package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class DashboardChartResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineData {
        private String label;
        private long created;
        private long solved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiData {
        private String avgFirstReplyTime; // e.g. "30h 15min"
        private String avgFullResolveTime;
        private long newTickets;
        private long returnedTickets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStats {
        private Map<String, Long> counts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStats {
        private Map<String, Long> counts; // Mon, Tue, Wed, etc.
    }
}
