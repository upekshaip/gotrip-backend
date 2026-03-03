package com.gotrip.experience_service.dto;

import java.time.LocalDateTime;

public record ExperienceResponseDTO(
        Long experienceId,
        String title,
        String description,
        String category,
        String type,
        String location,
        double pricePerUnit,
        String priceUnit,
        int maxCapacity,
        String imageUrl,
        boolean available,
        Long providerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
