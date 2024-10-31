package com.seva.propert.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ErrorDetail {
	@JsonProperty("time")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStamp;
	private Integer status;
	private String error;
	private Object message;
	
}
