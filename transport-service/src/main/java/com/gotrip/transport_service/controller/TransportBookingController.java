package com.gotrip.transport_service.controller;
import com.gotrip.common_library.dto.transport_service.TransportBookingRequest;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.transport_service.service.TransportBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transport-bookings")
@RequiredArgsConstructor
public class TransportBookingController {

    private final TransportBookingService bookingService;

    // Traveler requests a vehicle
    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody TransportBookingRequest req, Authentication auth) {
        return ResponseEntity.ok(bookingService.createBookingRequest(req, auth));
    }

    // Provider accepts or declines the request
    @PatchMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id,
                                     @RequestParam BookingStatus status,
                                     @RequestParam String message,
                                     Authentication auth) {
        return ResponseEntity.ok(bookingService.respondToBooking(id, status, message, auth));
    }

    // Traveler cancels their pending/accepted booking
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, auth));
    }
}