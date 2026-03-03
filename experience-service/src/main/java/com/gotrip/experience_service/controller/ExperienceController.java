package com.gotrip.experience_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.service.ExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/create")
    public ResponseEntity<?> createExperience(
            Authentication authentication,
            @Valid @RequestBody CreateExperienceRequest request) {
        try {
            Long providerId = extractUserId(authentication);
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
            Long providerId = extractUserId(authentication);
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
            Long providerId = extractUserId(authentication);
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
        try {
            ExperienceResponseDTO response = experienceService.getExperienceById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllExperiences() {
        return ResponseEntity.ok(experienceService.getAllExperiences());
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableExperiences() {
        return ResponseEntity.ok(experienceService.getAvailableExperiences());
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
    public ResponseEntity<?> getMyListings(Authentication authentication) {
        try {
            Long providerId = extractUserId(authentication);
            return ResponseEntity.ok(experienceService.getExperiencesByProvider(providerId));
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
