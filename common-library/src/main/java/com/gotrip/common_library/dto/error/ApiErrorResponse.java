package com.gotrip.common_library.dto.error;

public record ApiErrorResponse(String message,
                               int status,
                               long timestamp) {

}
