package com.reliaquest.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class EmployeeExceptionHandler {

    @ExceptionHandler(UnableToObtainEmployeesException.class)
    public ResponseEntity<ErrorResponse> handleCouldNotObtainEmployees(
            final UnableToObtainEmployeesException exception, final WebRequest webRequest) {
        return new ResponseEntity<>(
                new ErrorResponse(
                        webRequest.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(
            final EmployeeNotFoundException exception, final WebRequest webRequest) {
        return new ResponseEntity<>(
                new ErrorResponse(webRequest.getDescription(false), HttpStatus.NOT_FOUND, exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }
}
