package com.gotrip.experience_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            Long travellerId = extractUserId(authentication);
            BookingResponseDTO response = bookingService.createBooking(request, travellerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @PatchMapping("/{bookingId}/action")
    public ResponseEntity<?> providerAction(
            Authentication authentication,
            @PathVariable Long bookingId,
            @RequestBody ProviderActionDTO actionDTO) {
        try {
            Long providerId = extractUserId(authentication);
            BookingResponseDTO response = bookingService.providerAction(bookingId, actionDTO, providerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            Authentication authentication,
            @PathVariable Long bookingId) {
        try {
            Long travellerId = extractUserId(authentication);
            BookingResponseDTO response = bookingService.cancelBooking(bookingId, travellerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
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
    public ResponseEntity<?> getMyBookings(Authentication authentication) {
        try {
            Long travellerId = extractUserId(authentication);
            return ResponseEntity.ok(bookingService.getBookingsByTraveller(travellerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/provider/all")
    public ResponseEntity<?> getProviderBookings(Authentication authentication) {
        try {
            Long providerId = extractUserId(authentication);
            return ResponseEntity.ok(bookingService.getBookingsByProvider(providerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/provider/pending")
    public ResponseEntity<?> getPendingBookings(Authentication authentication) {
        try {
            Long providerId = extractUserId(authentication);
            return ResponseEntity.ok(bookingService.getPendingBookingsByProvider(providerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
        return Long.valueOf(principal.get("userId").toString());
    }
}
