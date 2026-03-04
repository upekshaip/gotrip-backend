package com.gotrip.hotel_service.controller;


import com.gotrip.common_library.dto.hotel_service.HotelBookingRequest;
import com.gotrip.common_library.dto.hotel_service.HotelBookingResponse;
import com.gotrip.common_library.dto.hotel_service.HotelRespondDTO;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.hotel_service.model.HotelBooking;
import com.gotrip.hotel_service.service.HotelBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotel-booking")
@RequiredArgsConstructor
public class HotelBookingController {

    private final HotelBookingService bookingService;

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody HotelBookingRequest req, Authentication auth) {
        return ResponseEntity.ok(bookingService.createBookingRequest(req, auth));
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id,
                                     @RequestBody HotelRespondDTO hotelRespondDTO,
//                                     @RequestParam BookingStatus status,
//                                     @RequestParam String message,
                                     Authentication auth) {
        return ResponseEntity.ok(bookingService.respondToBooking(id, hotelRespondDTO, auth));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, auth));
    }


    @GetMapping("/my-bookings")
    public ResponseEntity<Page<HotelBookingResponse>> getMyTrips(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(bookingService.getTravellerBookings(status, page, limit, auth));
    }

    // Fixed: Changed from Page<HotelBooking> to Page<HotelBookingResponse>
    @GetMapping("/incoming-requests")
    public ResponseEntity<Page<HotelBookingResponse>> getIncomingRequests(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(bookingService.getProviderBookings(status, page, limit, auth));
    }
}