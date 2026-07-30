package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionSourcesMetaView {
    private String sourceDonnees;
    private String datasetVersion;
    private String modelVersion;
    private String modelName;
    private String pipelineName;
    private Integer featureCount;
    private List<String> features;
    private String dataType;
    private Boolean synthetic;
    private String disclaimer;
    private String usageLimit;
}
