package com.gotrip.hotel_service.controller;


import com.gotrip.common_library.config.AppConfig;
import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.common_library.dto.user.UserProfileUpdateRequest;
import com.gotrip.hotel_service.service.HotelService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/hotel-service")
public class HotelServiceController {

    private final ObjectMapper objectMapper;
//    private final HotelService hotelService;

    public HotelServiceController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
//        this.hotelService = hotelService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        System.out.println("request coming...");
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
