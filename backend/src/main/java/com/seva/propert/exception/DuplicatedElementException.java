package com.seva.propert.exception;

public class DuplicatedElementException extends RuntimeException {

    public DuplicatedElementException() {
        super("Duplicated element.");
    }

    public DuplicatedElementException(String message) {
        super(message);
    }

    public DuplicatedElementException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedElementException(Throwable cause) {
        super(cause);
    }
}
