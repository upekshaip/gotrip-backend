package ackage com.gotrip.review_service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a user review for a transport service.
 * Includes validation for ratings and comment length to ensure data quality.
 */
public record ReviewDTO(
        Long id,

        @NotNull(message = "Transport ID cannot be null")
        Long transportId,

        @NotBlank(message = "User identification is required")
        String userId,

        @Min(value = 1, message = "Rating must be at least 1 star")
        @Max(value = 5, message = "Rating cannot exceed 5 stars")
        int rating,

        @Size(max = 1000, message = "Review comment must be under 1000 characters")
        String comment,

        LocalDateTime createdAt
) {
    System.out.println("shakeef");
    public boolean isPositive() {
        return this.rating >= 4;
    }
}