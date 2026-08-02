package com.pfa.tracabilite_ia.exception;

import com.pfa.tracabilite_ia.dto.response.ApiErrorResponse;
import com.pfa.tracabilite_ia.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, "NOT_FOUND", safeMessage(ex.getMessage(), "Ressource introuvable"), req);
    }

    @ExceptionHandler(MLServiceValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMlValidation(MLServiceValidationException ex, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", safeMessage(ex.getMessage(), "Requête invalide"), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Corps de requête invalide", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiErrorResponse.FieldErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiErrorResponse.FieldErrorItem(err.getField(), err.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        "Requête invalide",
                        correlationId(req),
                        errors
                )
        );
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiErrorResponse> handleRestClient(RestClientException ex, HttpServletRequest req) {
        log.warn("Service distant indisponible: {}", ex.getClass().getSimpleName());
        return respond(HttpStatus.SERVICE_UNAVAILABLE, "ML_UNAVAILABLE", "Service ML indisponible", req);
    }

    @ExceptionHandler(OpenRouterException.class)
    public ResponseEntity<ApiErrorResponse> handleOpenRouter(OpenRouterException ex, HttpServletRequest req) {
        HttpStatus status = ex.getHttpStatus() > 0
                ? HttpStatus.valueOf(ex.getHttpStatus())
                : HttpStatus.BAD_GATEWAY;
        return respond(status, ex.getErrorCode().name(), safeMessage(ex.getMessage(), "Erreur agents IA"), req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return respond(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Identifiants invalides", req);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedActionException ex, HttpServletRequest req) {
        return respond(HttpStatus.FORBIDDEN, "FORBIDDEN", safeMessage(ex.getMessage(), "Action non autorisée"), req);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest req) {
        return respond(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Accès refusé — rôle insuffisant", req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return respond(HttpStatus.CONFLICT, "CONFLICT", safeMessage(ex.getMessage(), "Conflit d'état"), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", safeMessage(ex.getMessage(), "Requête invalide"), req);
    }

    @ExceptionHandler(AIServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleAiUnavailable(AIServiceUnavailableException ex, HttpServletRequest req) {
        return respond(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "Service IA indisponible", req);
    }

    @ExceptionHandler(AIInvalidResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleAiInvalidResponse(AIInvalidResponseException ex, HttpServletRequest req) {
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, "AI_INVALID_RESPONSE", "Réponse IA invalide", req);
    }

    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAiService(AIServiceException ex, HttpServletRequest req) {
        log.error("Erreur service IA", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "AI_ERROR", "Erreur service IA", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erreur interne non gérée", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Erreur interne", req);
    }

    private static ResponseEntity<ApiErrorResponse> respond(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(status).body(
                ApiErrorResponse.of(status.value(), code, message, correlationId(req))
        );
    }

    private static String correlationId(HttpServletRequest req) {
        String fromMdc = org.slf4j.MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc;
        }
        String header = req.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        return header != null ? header : null;
    }

    private static String safeMessage(String message, String fallback) {
        if (message == null || message.isBlank()) {
            return fallback;
        }
        // Empêche la fuite de chemins / SQL dans les réponses
        String lower = message.toLowerCase();
        if (lower.contains("jdbc:") || lower.contains("password") || lower.contains("exception")
                || lower.contains("stack") || lower.contains("hibernate") || lower.contains("sql")) {
            return fallback;
        }
        return message.length() > 300 ? fallback : message;
    }
}
