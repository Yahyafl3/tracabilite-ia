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
public class GroqStatusResponse {
    private boolean configured;
    private boolean reachable;
    private String lastError;
    private List<GroqModelStatusResponse> models;
}
