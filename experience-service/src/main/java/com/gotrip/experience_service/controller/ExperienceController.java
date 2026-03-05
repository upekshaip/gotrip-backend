package com.gotrip.experience_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.service.ExperienceService;
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
@RequestMapping("/experience")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;
    private final BookingService bookingService;

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getAdminStats(Authentication authentication) {
        Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
        if (!(boolean) principal.getOrDefault("admin", false)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiErrorResponse("Unauthorized: Only admins can access stats.", HttpStatus.FORBIDDEN.value(), System.currentTimeMillis())
            );
        }
        long totalExperiences = experienceService.countAll();
        long availableExperiences = experienceService.countAvailable();
        long totalBookings = bookingService.countAll();
        long pendingBookings = bookingService.countPending();
        return ResponseEntity.ok(Map.of(
                "totalExperiences", totalExperiences,
                "availableExperiences", availableExperiences,
                "totalBookings", totalBookings,
                "pendingBookings", pendingBookings
        ));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createExperience(
            Authentication authentication,
            @Valid @RequestBody CreateExperienceRequest request) {
        try {
            Long providerId = extractProviderId(authentication);
            ExperienceResponseDTO response = experienceService.createExperience(request, providerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateExperience(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateExperienceRequest request) {
        try {
            Long providerId = extractProviderId(authentication);
            ExperienceResponseDTO response = experienceService.updateExperience(id, request, providerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExperience(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            Long providerId = extractProviderId(authentication);
            experienceService.deleteExperience(id, providerId);
            return ResponseEntity.ok(Map.of("message", "Experience deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExperienceById(@PathVariable Long id) {
            Map<String, Object> response = experienceService.getExperienceById(id);
            return ResponseEntity.ok(response);

    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllExperiences() {
        return ResponseEntity.ok(experienceService.getAllExperiences());
    }

    @GetMapping("/available")
    public ResponseEntity<Page<ExperienceResponseDTO>> getAvailableExperiences(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        // page - 1 because internal Spring Data logic is 0-indexed
        Page<ExperienceResponseDTO> result = experienceService.getAvailableExperiences(page, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(experienceService.getExperiencesByCategory(category));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<?> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(experienceService.getExperiencesByLocation(location));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<Page<ExperienceResponseDTO>> getMyListings(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit)
    {
        Long providerId = extractProviderId(authentication);
        Page<ExperienceResponseDTO> result = experienceService.getExperiencesByProvider(providerId, page, limit);
        return ResponseEntity.ok(result);

    }

    @SuppressWarnings("unchecked")
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
