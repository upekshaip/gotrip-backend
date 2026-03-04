package com.gotrip.experience_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.experience_service.dto.CreateReviewRequest;
import com.gotrip.experience_service.dto.ReviewResponseDTO;
import com.gotrip.experience_service.dto.ReviewSummaryDTO;
import com.gotrip.experience_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/experience/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        try {
            Long travellerId = extractTravellerId(authentication);
            ReviewResponseDTO response = reviewService.createReview(request, travellerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            Authentication authentication,
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request) {
        try {
            Long travellerId = extractTravellerId(authentication);
            ReviewResponseDTO response = reviewService.updateReview(reviewId, request, travellerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            Authentication authentication,
            @PathVariable Long reviewId) {
        try {
            Long travellerId = extractTravellerId(authentication);
            reviewService.deleteReview(reviewId, travellerId);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/experience/{experienceId}")
    public ResponseEntity<?> getReviewsByExperience(@PathVariable Long experienceId) {
        try {
            List<ReviewResponseDTO> reviews = reviewService.getReviewsByExperience(experienceId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        try {
            Long travellerId = extractTravellerId(authentication);
            List<ReviewResponseDTO> reviews = reviewService.getMyReviews(travellerId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/summary/{experienceId}")
    public ResponseEntity<?> getReviewSummary(@PathVariable Long experienceId) {
        try {
            ReviewSummaryDTO summary = reviewService.getReviewSummary(experienceId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
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
