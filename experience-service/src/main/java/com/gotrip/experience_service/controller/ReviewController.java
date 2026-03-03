package com.gotrip.experience_service.controller;

import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.experience_service.dto.CreateReviewRequest;
import com.gotrip.experience_service.dto.ReviewResponseDTO;
import com.gotrip.experience_service.dto.ReviewSummaryDTO;
import com.gotrip.experience_service.service.ReviewService;
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
            @RequestBody CreateReviewRequest request) {
        try {
            Long travellerId = extractUserId(authentication);
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
            @RequestBody CreateReviewRequest request) {
        try {
            Long travellerId = extractUserId(authentication);
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
            Long travellerId = extractUserId(authentication);
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
            Long travellerId = extractUserId(authentication);
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
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
        return Long.valueOf(principal.get("userId").toString());
    }
}
