package com.gotrip.hotel_service.controller;


import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.common_library.dto.hotel_service.HotelCreateRequest;
import com.gotrip.common_library.dto.hotel_service.HotelSummaryResponse;
import com.gotrip.common_library.dto.hotel_service.enums.HotelStatus;
import com.gotrip.hotel_service.service.HotelService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    private final HotelService hotelService;

    public HotelServiceController(ObjectMapper objectMapper, HotelService hotelService) {
        this.objectMapper = objectMapper;
        this.hotelService = hotelService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody HotelCreateRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.createHotel(req, auth));
    }

    @GetMapping
    public ResponseEntity<Page<HotelSummaryResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(hotelService.getAllActive(page, limit));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyHotels(
            @RequestParam(required = false) HotelStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(hotelService.getMyAll(status, page, limit, auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getById(id));
    }

    @GetMapping("traveller/{id}")
    public ResponseEntity<?> getByTraveller(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getByIdForTraveller(id));
    }


    @GetMapping("/provider/{id}")
    public ResponseEntity<?> getByProvider(@PathVariable Long id, @Valid @RequestBody HotelCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(hotelService.getByIdForProvider(id, auth));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody HotelCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(hotelService.update(id, req, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(hotelService.delete(id, auth));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        System.out.println("request coming...");
        return ResponseEntity.ok(authentication.getPrincipal());

    }
}
