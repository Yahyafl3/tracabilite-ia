package com.pfa.tracabilite_ia.exception;

import org.springframework.http.HttpHeaders;

public class GroqException extends RuntimeException {

    private final GroqErrorCode errorCode;
    private final int httpStatus;
    private final HttpHeaders responseHeaders;

    public GroqException(GroqErrorCode errorCode, String message) {
        this(errorCode, message, 0, null, null);
    }

    public GroqException(GroqErrorCode errorCode, String message, int httpStatus, Throwable cause) {
        this(errorCode, message, httpStatus, cause, null);
    }

    public GroqException(GroqErrorCode errorCode, String message, int httpStatus, Throwable cause,
                         HttpHeaders responseHeaders) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.responseHeaders = responseHeaders;
    }

    public GroqErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }
}
