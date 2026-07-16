package com.pfa.tracabilite_ia.exception;

public class AIServiceUnavailableException extends AIServiceException {

    public AIServiceUnavailableException(String message) {
        super(message);
    }

    public AIServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
