package com.pfa.tracabilite_ia.exception;

public class OpenRouterException extends RuntimeException {

    private final OpenRouterErrorCode errorCode;
    private final int httpStatus;
    private final org.springframework.http.HttpHeaders responseHeaders;

    public OpenRouterException(OpenRouterErrorCode errorCode, String message) {
        this(errorCode, message, 0, null, null);
    }

    public OpenRouterException(OpenRouterErrorCode errorCode, String message, int httpStatus, Throwable cause) {
        this(errorCode, message, httpStatus, cause, null);
    }

    public OpenRouterException(OpenRouterErrorCode errorCode, String message, int httpStatus,
                               Throwable cause, org.springframework.http.HttpHeaders responseHeaders) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.responseHeaders = responseHeaders;
    }

    public OpenRouterErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public org.springframework.http.HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }
}
