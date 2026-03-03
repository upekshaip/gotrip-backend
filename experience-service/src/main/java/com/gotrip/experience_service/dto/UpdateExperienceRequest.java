package com.gotrip.experience_service.dto;

public record UpdateExperienceRequest(
        String title,
        String description,
        String category,
        String type,
        String location,
        Double pricePerUnit,
        String priceUnit,
        Integer maxCapacity,
        String imageUrl,
        Boolean available
) {}
