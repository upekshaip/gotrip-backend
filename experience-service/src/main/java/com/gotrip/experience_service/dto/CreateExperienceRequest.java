package com.gotrip.experience_service.dto;

import jakarta.validation.constraints.*;

public record CreateExperienceRequest(
        @NotBlank(message = "Title is required")
        String title,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @NotBlank(message = "Category is required")
        String category,

        @NotBlank(message = "Type is required")
        String type,

        @NotBlank(message = "Location is required")
        String location,

        @Positive(message = "Price must be positive")
        double pricePerUnit,

        String priceUnit,

        @Min(value = 1, message = "Max capacity must be at least 1")
        int maxCapacity,

        String imageUrl
) {}
