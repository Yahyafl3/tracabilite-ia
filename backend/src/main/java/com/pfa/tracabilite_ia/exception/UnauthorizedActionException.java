package com.pfa.tracabilite_ia.exception;

public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException() {
        super();
    }

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
