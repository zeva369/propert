package com.sidus.propert.exception;

public class InvalidUserIdException extends RuntimeException {
    public InvalidUserIdException() {
        super("Invalid user ID.");
    }

    public InvalidUserIdException(String message) {
        super(message);
    }

    public InvalidUserIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUserIdException(Throwable cause) {
        super(cause);
    }
}
