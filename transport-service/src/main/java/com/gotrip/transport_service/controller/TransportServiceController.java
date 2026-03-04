package com.gotrip.transport_service.controller;
import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.common_library.dto.transport_service.TransportCreateRequest;
import com.gotrip.transport_service.service.TransportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/transport-service")
public class TransportServiceController {

    private final ObjectMapper objectMapper;
    private final TransportService transportService;

    public TransportServiceController(TransportService transportService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.transportService = transportService;
    }

    // Vehicle Management Endpoints

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TransportCreateRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportService.createTransport(req, auth));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(transportService.getAllActive());
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

    //  Utility Endpoints

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        try {
            if (authentication == null) {
                throw new Exception("Authorization is null");
            }
            return ResponseEntity.ok(authentication.getPrincipal());
        } catch (Exception e) {
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