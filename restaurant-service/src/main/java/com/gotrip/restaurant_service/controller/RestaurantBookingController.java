package com.gotrip.restaurant_service.controller;


import com.gotrip.common_library.dto.restaurant_service.RestaurantBookingRequest;
import com.gotrip.common_library.dto.restaurant_service.RestaurantBookingResponse;
import com.gotrip.common_library.dto.restaurant_service.RestaurantRespondDTO;
import com.gotrip.common_library.dto.restaurant_service.enums.BookingStatus;
import com.gotrip.restaurant_service.model.RestaurantBooking;
import com.gotrip.restaurant_service.service.RestaurantBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurant-booking")
@RequiredArgsConstructor
public class RestaurantBookingController {

    private final RestaurantBookingService bookingService;

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody RestaurantBookingRequest req, Authentication auth) {
        return ResponseEntity.ok(bookingService.createBookingRequest(req, auth));
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id,
                                     @RequestBody RestaurantRespondDTO restaurantRespondDTO,
//                                     @RequestParam BookingStatus status,
//                                     @RequestParam String message,
                                     Authentication auth) {
        return ResponseEntity.ok(bookingService.respondToBooking(id, restaurantRespondDTO, auth));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, auth));
    }


    @GetMapping("/my-bookings")
    public ResponseEntity<Page<RestaurantBookingResponse>> getMyTrips(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        System.out.println("hit");
        return ResponseEntity.ok(bookingService.getTravellerBookings(status, page, limit, auth));
    }

    // Fixed: Changed from Page<RestaurantBooking> to Page<RestaurantBookingResponse>
    @GetMapping("/incoming-requests")
    public ResponseEntity<Page<RestaurantBookingResponse>> getIncomingRequests(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(bookingService.getProviderBookings(status, page, limit, auth));
    }
}