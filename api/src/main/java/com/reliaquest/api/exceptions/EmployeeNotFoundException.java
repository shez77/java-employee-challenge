package com.reliaquest.api.exceptions;

import org.springframework.web.client.HttpClientErrorException;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(final String message) {
        super(message);
    }

    public EmployeeNotFoundException(String message, HttpClientErrorException e) {
        super(message, e);
    }
}
