package com.pfa.tracabilite_ia.exception;

/**
 * Thrown when login email/password are invalid.
 * Mapped to HTTP 401 by {@link GlobalExceptionHandler}.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Identifiants invalides");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
