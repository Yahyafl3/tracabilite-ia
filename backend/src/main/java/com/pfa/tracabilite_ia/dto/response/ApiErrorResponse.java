package com.pfa.tracabilite_ia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String correlationId,
        List<FieldErrorItem> errors
) {
    public record FieldErrorItem(String field, String message) {}

    public static ApiErrorResponse of(int status, String code, String message, String correlationId) {
        return new ApiErrorResponse(Instant.now(), status, code, message, correlationId, null);
    }

    public static ApiErrorResponse of(
            int status,
            String code,
            String message,
            String correlationId,
            List<FieldErrorItem> errors
    ) {
        return new ApiErrorResponse(Instant.now(), status, code, message, correlationId, errors);
    }
}
