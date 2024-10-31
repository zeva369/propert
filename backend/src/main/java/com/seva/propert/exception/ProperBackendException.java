package com.seva.propert.exception;

import org.springframework.http.HttpStatus;

import lombok.Data;


@Data
public class ProperBackendException extends RuntimeException {
	
	private HttpStatus httpStatus;
	
	public ProperBackendException (HttpStatus httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProperBackendException other = (ProperBackendException) obj;
        if (httpStatus != other.httpStatus) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((httpStatus == null) ? 0 : httpStatus.hashCode());
        return result;
    }
}
