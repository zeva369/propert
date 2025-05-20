package com.sidus.propert.context.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle( HttpServletRequest request,  HttpServletResponse response,  AccessDeniedException ex) throws IOException, ServletException { 
    	log.error("Pasa por el handle del Custom Access Denied Handler");
    	String responseString = "{\"status\":\"403\"}";
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.getOutputStream().write(responseString.getBytes(StandardCharsets.UTF_8));
    }
}