package com.sidus.propert.exception;

public class TaskPredecessorNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TaskPredecessorNotFoundException() {
        super("Task predecessor not found.");
    }

    public TaskPredecessorNotFoundException(String message) {
        super(message);
    }

    public TaskPredecessorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskPredecessorNotFoundException(Throwable cause) {
        super(cause);
    }
}
