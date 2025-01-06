package com.reliaquest.api.exceptions;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(final String message) {
        super(message);
    }
}
