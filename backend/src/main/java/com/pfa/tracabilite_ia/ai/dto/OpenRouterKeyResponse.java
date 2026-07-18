package com.pfa.tracabilite_ia.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenRouterKeyResponse {

    private KeyData data;

    public KeyData getData() {
        return data;
    }

    public void setData(KeyData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyData {
        private Double usage;
        private Double limit;

        @JsonProperty("is_free_tier")
        private Boolean freeTier;

        public Double getUsage() {
            return usage;
        }

        public void setUsage(Double usage) {
            this.usage = usage;
        }

        public Double getLimit() {
            return limit;
        }

        public void setLimit(Double limit) {
            this.limit = limit;
        }

        public Boolean getFreeTier() {
            return freeTier;
        }

        public void setFreeTier(Boolean freeTier) {
            this.freeTier = freeTier;
        }
    }
}
