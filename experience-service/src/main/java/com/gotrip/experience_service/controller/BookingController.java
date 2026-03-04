package com.gotrip.experience_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/experience/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/request")
    public ResponseEntity<?> createBooking(
            Authentication authentication,
            @Valid @RequestBody BookingRequestDTO request) {
        try {
            Long travellerId = extractTravellerId(authentication);
            BookingResponseDTO response = bookingService.createBooking(request, travellerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @PatchMapping("/{bookingId}/respond")
    public ResponseEntity<?> providerAction(
            Authentication authentication,
            @PathVariable Long bookingId,
            @RequestBody ProviderActionDTO actionDTO) {
        try {
            Long providerId = extractProviderId(authentication);
            BookingResponseDTO response = bookingService.providerAction(bookingId, actionDTO, providerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @DeleteMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(Authentication authentication, @PathVariable Long bookingId) {

            BookingResponseDTO response = bookingService.cancelBooking(bookingId, authentication);
            return ResponseEntity.ok(response);

    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        try {
            BookingResponseDTO response = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(Authentication authentication, Pageable pageable) {
        Page<ProviderBookingDetailDTO> bookings = bookingService.getBookingsByTraveller(authentication, pageable);
        return ResponseEntity.ok(bookings);

    }

    @GetMapping("/provider/all")
    public ResponseEntity<?> getProviderBookings(Authentication authentication,Pageable pageable) { // Spring injects this from query params
            Page<ProviderBookingDetailDTO> bookings = bookingService.getBookingsByProvider(authentication, pageable);
            return ResponseEntity.ok(bookings);
    }

    @GetMapping("/provider/pending")
    public ResponseEntity<?> getPendingBookings(Authentication authentication) {
        try {
            Long providerId = extractProviderId(authentication);
            return ResponseEntity.ok(bookingService.getPendingBookingsByProvider(providerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    private Long extractProviderId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("serviceProvider", false)) {
            throw new RuntimeException("Unauthorized: You are not authorized to perform this operation.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        return ((Number) profile.get("providerId")).longValue();
    }

    private Long extractTravellerId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("traveller", false)) {
            throw new RuntimeException("Unauthorized: You are not authorized to perform this operation..");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");
        return ((Number) profile.get("travellerId")).longValue();
    }
}
