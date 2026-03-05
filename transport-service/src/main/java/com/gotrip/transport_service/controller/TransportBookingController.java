package com.gotrip.transport_service.controller;

import com.gotrip.common_library.dto.transport_service.TransportBookingRequest;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.transport_service.service.TransportBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/transport-service/bookings")
@RequiredArgsConstructor
public class TransportBookingController {

    private final TransportBookingService bookingService;

    // --- Core Booking Actions ---

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody TransportBookingRequest req, Authentication auth) {
        return ResponseEntity.ok(bookingService.createBookingRequest(req, auth));
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id,
                                     @RequestParam BookingStatus status,
                                     @RequestParam String message,
                                     Authentication auth) {
        return ResponseEntity.ok(bookingService.respondToBooking(id, status, message, auth));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, auth));
    }

    //  Complete the ride (so they can review it later)
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(bookingService.completeBooking(id, auth));
    }

    // --- Frontend Data Retrieval ---

    // Get all bookings for the logged-in Traveler
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(bookingService.getMyBookings(status, page, limit, auth));
    }

    //  Get all incoming requests for the logged-in Provider
    @GetMapping("/provider-requests")
    public ResponseEntity<?> getProviderRequests(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(bookingService.getProviderBookings(status, page, limit, auth));
    }
}