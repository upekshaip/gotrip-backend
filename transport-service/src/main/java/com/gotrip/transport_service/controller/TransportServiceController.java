package com.gotrip.transport_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.common_library.dto.transport_service.TransportCreateRequest;
import com.gotrip.transport_service.service.TransportService;
import com.gotrip.transport_service.service.TransportBookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    private final TransportService transportService;
    private final TransportBookingService transportBookingService;

    public TransportServiceController(TransportService transportService, TransportBookingService transportBookingService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.transportService = transportService;
        this.transportBookingService = transportBookingService;
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getAdminStats(Authentication authentication) {
        try {
            Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
            if (!(boolean) principal.getOrDefault("admin", false)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiErrorResponse("Unauthorized: Only admins can access stats.", HttpStatus.FORBIDDEN.value(), System.currentTimeMillis())
                );
            }
            long totalTransports = transportService.countAll();
            long activeTransports = transportService.countActive();
            long totalBookings = transportBookingService.countAll();
            long pendingBookings = transportBookingService.countPending();
            return ResponseEntity.ok(Map.of(
                    "totalTransports", totalTransports,
                    "activeTransports", activeTransports,
                    "totalBookings", totalBookings,
                    "pendingBookings", pendingBookings
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TransportCreateRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportService.createTransport(req, auth));
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(transportService.getAllActive(page, limit));
    }

    //  The Search Endpoint for the Frontend!
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(transportService.searchTransports(city, page, limit));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTransports(
            @RequestParam(required = false) com.gotrip.common_library.dto.transport_service.enums.TransportStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(transportService.getMyAll(status, page, limit, auth));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllTransportsByAdmin(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(transportService.getAllTransportsByAdmin(authentication, page, limit));
    }

    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingTransportsByAdmin(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(transportService.getPendingTransportsByAdmin(authentication, page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(transportService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TransportCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(transportService.update(id, req, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        transportService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/admin/{id}/approve")
    public ResponseEntity<?> approveTransport(@PathVariable Long id, Authentication authentication) {
        try {
            return ResponseEntity.ok(transportService.approveTransport(id, authentication));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        try {
            if (authentication == null) {
                throw new Exception("Authorization is null");
            }
            return ResponseEntity.ok(authentication.getPrincipal());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }
}