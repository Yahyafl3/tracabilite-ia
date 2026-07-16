package com.pfa.tracabilite_ia.exception;

public class OpenRouterException extends RuntimeException {

    private final OpenRouterErrorCode errorCode;
    private final int httpStatus;

    public OpenRouterException(OpenRouterErrorCode errorCode, String message) {
        this(errorCode, message, 0, null);
    }

    public OpenRouterException(OpenRouterErrorCode errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public OpenRouterErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
