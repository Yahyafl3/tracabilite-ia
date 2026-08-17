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
        private double approvalRate;
        private long highRiskCount;
        /** Dossiers en attente d'arbitrage humain (brouillon inclus). */
        private long pendingValidation;
        /** Dossiers signés dont l'empreinte SHA-256 se recalcule à l'identique. */
        private long integrityVerified;
        /** Dossiers portant une empreinte, donc vérifiables. */
        private long integrityTotal;
        /** Validations humaines ayant confirmé la suggestion de l'IA. */
        private long aiAgreement;
        /** Validations humaines s'étant écartées de la suggestion de l'IA. */
        private long aiDisagreement;
        /** Dossiers pas encore arbitrés par un humain. */
        private long aiNotArbitrated;
        private Map<String, Object> domainMetrics;
        private Map<String, Long> riskBreakdown;
        private Map<String, List<Double>> sparklines;
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
