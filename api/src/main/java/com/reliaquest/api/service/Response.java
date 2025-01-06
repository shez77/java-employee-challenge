package com.reliaquest.api.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Package protected Response representation from the Mock Employee Service.
 *
 * @param data
 * @param status
 * @param error
 * @param <T>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
record Response<T>(T data, Status status, String error) {
    enum Status {
        HANDLED("Successfully processed request."),
        ERROR("Failed to process request.");

        @JsonValue
        @Getter
        private final String value;

        Status(String value) {
            this.value = value;
        }
    }
}
