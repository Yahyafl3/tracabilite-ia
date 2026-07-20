package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroqModelStatusResponse {
    private String agent;
    private String displayName;
    private String modelId;
    private boolean available;
}
