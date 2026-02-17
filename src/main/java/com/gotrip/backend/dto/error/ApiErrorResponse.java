package com.gotrip.backend.dto.error;

public record ApiErrorResponse(String message,
                               int status,
                               long timestamp) {

}
