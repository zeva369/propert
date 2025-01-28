package com.seva.propert.context.security.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.seva.propert.exception.ErrorDetail;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exception(Exception exception) throws Exception {
        if (exception instanceof AccessDeniedException
                || exception instanceof AuthenticationException) {
            throw exception;
        }
        log.error(exception != null ? exception.getMessage():"error:unknown");
        ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		body.setMessage(exception.getMessage());
        return ResponseEntity.internalServerError().body(body);
    }

}