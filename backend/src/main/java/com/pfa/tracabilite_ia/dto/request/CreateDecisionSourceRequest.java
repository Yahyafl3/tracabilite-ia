package com.pfa.tracabilite_ia.dto.request;

import com.pfa.tracabilite_ia.enumeration.DecisionSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CreateDecisionSourceRequest {

    @NotNull
    private DecisionSourceType sourceType;

    @NotBlank
    private String name;

    private String description;
    private String url;
    private String documentReference;
    private Map<String, Object> metadata;
}
