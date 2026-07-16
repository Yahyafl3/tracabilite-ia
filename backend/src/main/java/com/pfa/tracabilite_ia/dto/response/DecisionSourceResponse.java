package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.DecisionSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionSourceResponse {
    private UUID sourceId;
    private UUID decisionId;
    private DecisionSourceType sourceType;
    private String name;
    private String description;
    private String url;
    private String documentReference;
    private String contentHash;
    private Map<String, Object> metadata;
    private UUID createdById;
    private String createdByEmail;
    private LocalDateTime createdAt;
}
