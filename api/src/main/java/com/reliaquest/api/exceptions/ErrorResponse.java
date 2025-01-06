package com.reliaquest.api.exceptions;

import org.springframework.http.HttpStatus;

/**
 * An Error Response Type
 * @param apiPath
 * @param errorCode
 * @param errorMessage
 */
public record ErrorResponse(String apiPath, HttpStatus errorCode, String errorMessage) {}
