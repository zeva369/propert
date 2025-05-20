package com.sidus.propert.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.sidus.propert.context.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.RequiredTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
@RequiredArgsConstructor
public class ProperBackendExceptionHandler extends ResponseEntityExceptionHandler{

	private final ErrorMessages errorMessages;

	private final ObjectMapper mapper = new ObjectMapper();
	
	@ExceptionHandler(ValidationException.class)
	protected ResponseEntity<Object> handleConflict(ValidationException ex, WebRequest request){
		List<JsonNode> errors = ex.getResult().getFieldErrors()
		.stream()
		.map(fieldError -> {
			ObjectNode node = mapper.createObjectNode();
		    node.put("field", fieldError.getField());
		    node.put("message", fieldError.getDefaultMessage());
		    return node;
		})
		.collect(Collectors.toList());

		
		ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(HttpStatus.BAD_REQUEST.value());
		body.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.setMessage(errors);
		
		return handleExceptionInternal(ex,body,new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	// This method is called when a request body anottated with @Valid fails
	// validation
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {

		List<JsonNode> errors = ex.getBindingResult().getFieldErrors()
				.stream()
				.map(fieldError -> {
					ObjectNode node = mapper.createObjectNode();
					node.put("field", fieldError.getField());
					node.put("message", fieldError.getDefaultMessage());
					return node;
				})
				.collect(Collectors.toList());

		ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(HttpStatus.BAD_REQUEST.value());
		body.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.setMessage(errors);

		return handleExceptionInternal(ex,body,new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(WorkFlowLoopException.class)
	protected ResponseEntity<?> handleConflict(WorkFlowLoopException ex, WebRequest request){

		ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(HttpStatus.CONFLICT.value());
		body.setError(HttpStatus.CONFLICT.getReasonPhrase());
		body.setMessage(errorMessages.WORKFLOW_LOOP);

		return handleExceptionInternal(ex,body,new HttpHeaders(),HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler(InvalidUserIdException.class)
	protected ResponseEntity<?> handleConflict(InvalidUserIdException ex, WebRequest request){
		log.error("Invalid user id: {}", ex.getMessage());
		ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(HttpStatus.BAD_REQUEST.value());
		body.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.setMessage(ex.getMessage());

		return handleExceptionInternal(ex,body,new HttpHeaders(),HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(ProperBackendException.class)
	protected ResponseEntity<Object> handleConflict(ProperBackendException ex, WebRequest request){
		ErrorDetail body = new ErrorDetail();
		body.setTimeStamp(LocalDateTime.now());
		body.setStatus(ex.getHttpStatus().value());
		body.setError(ex.getHttpStatus().getReasonPhrase());
		body.setMessage(ex.getMessage());
		return handleExceptionInternal(ex,body,new HttpHeaders(),ex.getHttpStatus(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> exception(Exception exception) throws Exception {
		if (exception instanceof AccessDeniedException ||
				exception instanceof AuthenticationException) {
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