package com.sidus.propert.exception;

public class ElementNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public ElementNotFoundException() {
        super("Element not found.");
    }

    public ElementNotFoundException(String message) {
        super(message);
    }

    public ElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElementNotFoundException(Throwable cause) {
        super(cause);
    }
}
