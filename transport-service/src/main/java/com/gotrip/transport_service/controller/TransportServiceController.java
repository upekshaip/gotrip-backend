package com.gotrip.transport_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/transport-service")
public class TransportServiceController {

    private final ObjectMapper objectMapper;
//    private final TransportService transportService;

    public TransportServiceController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
//        this.transportService = transportService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        try {
        if (authentication == null) {
           throw new Exception("Authorization is null");
        }
        // This is the user Map we extracted from the JSON string in the JWT
        return ResponseEntity.ok(authentication.getPrincipal());
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(
                            e.getMessage(),
                            HttpStatus.BAD_REQUEST.value(),
                            System.currentTimeMillis()
                    )

            );
        }
    }
}

