package com.gotrip.camera_service.controller;


import com.gotrip.common_library.dto.camera_service.CameraBookingRequest;
import com.gotrip.common_library.dto.camera_service.CameraBookingResponse;
import com.gotrip.common_library.dto.camera_service.CameraRespondDTO;
import com.gotrip.common_library.dto.camera_service.enums.BookingStatus;
import com.gotrip.camera_service.service.CameraBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/camera-booking")
@RequiredArgsConstructor
public class CameraBookingController {

    private final CameraBookingService bookingService;

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody CameraBookingRequest req, Authentication auth) {
        return ResponseEntity.ok(bookingService.createBookingRequest(req, auth));
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id,
                                     @RequestBody CameraRespondDTO cameraRespondDTO,
//                                     @RequestParam BookingStatus status,
//                                     @RequestParam String message,
                                     Authentication auth) {
        return ResponseEntity.ok(bookingService.respondToBooking(id, cameraRespondDTO, auth));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, auth));
    }


    @GetMapping("/my-bookings")
    public ResponseEntity<Page<CameraBookingResponse>> getMyTrips(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(bookingService.getTravellerBookings(status, page, limit, auth));
    }

    // Fixed: Changed from Page<CameraBooking> to Page<CameraBookingResponse>
    @GetMapping("/incoming-requests")
    public ResponseEntity<Page<CameraBookingResponse>> getIncomingRequests(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(bookingService.getProviderBookings(status, page, limit, auth));
    }
}