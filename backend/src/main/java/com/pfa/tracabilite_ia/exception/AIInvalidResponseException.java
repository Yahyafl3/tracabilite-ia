package com.pfa.tracabilite_ia.exception;

public class AIInvalidResponseException extends AIServiceException {

    public AIInvalidResponseException(String message) {
        super(message);
    }

    public AIInvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
