package com.sidus.propert.exception;

public class InvalidTaskIdFormatException extends RuntimeException{

    public InvalidTaskIdFormatException() {
        super("Invalid task ID format");
    }

    public InvalidTaskIdFormatException(String message) {
        super(message);
    }

    public InvalidTaskIdFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTaskIdFormatException(Throwable cause) {
        super(cause);
    }


}
