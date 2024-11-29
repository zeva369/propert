package com.seva.propert.context.security;


import org.springframework.web.cors.DefaultCorsProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seva.propert.exception.ErrorDetail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;


@Slf4j
public class CustomCorsProcessor extends DefaultCorsProcessor { //implements CorsProcessor {

		@Override
		protected void rejectRequest(ServerHttpResponse response) throws IOException {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule());
			ErrorDetail body = new ErrorDetail();
			body.setTimeStamp(LocalDateTime.now());
			body.setStatus(HttpStatus.FORBIDDEN.value());
			body.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
			body.setMessage("Cors: Invalid Request, Origin not allowed");

			response.setStatusCode(HttpStatus.FORBIDDEN);
			response.getBody().write(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));

    		response.flush();
		}
		
	}

