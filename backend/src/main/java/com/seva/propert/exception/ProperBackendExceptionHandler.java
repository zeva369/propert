package com.seva.propert.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ProperBackendExceptionHandler extends ResponseEntityExceptionHandler{
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	@ExceptionHandler(ValidationException.class)
	protected ResponseEntity<?> handleConflict(ValidationException ex, WebRequest request){
		List<JsonNode> errors = ex.getResult().getFieldErrors()
		.stream()
		.map(fieldError -> {
			JsonNode node = mapper.createObjectNode();
		    ((ObjectNode) node).put("field", fieldError.getField());
		    ((ObjectNode) node).put("message", fieldError.getDefaultMessage());
		    return node;
		})
		.collect(Collectors.toList());

		
		ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(HttpStatus.BAD_REQUEST.value());
		body.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.setMessage(errors);
		
		return handleExceptionInternal(ex,body,new HttpHeaders(),ex.getHttpStatus(), request);
	}
	
	@ExceptionHandler(ProperBackendException.class)
	protected ResponseEntity<?> handleConflict(ProperBackendException ex, WebRequest request){
		ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(ex.getHttpStatus().value());
		body.setError(ex.getHttpStatus().getReasonPhrase());
		body.setMessage(ex.getMessage());
		return handleExceptionInternal(ex,body,new HttpHeaders(),ex.getHttpStatus(), request);
	}
}