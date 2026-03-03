package com.gotrip.experience_service.dto;

public record CreateExperienceRequest(
        String title,
        String description,
        String category,
        String type,
        String location,
        double pricePerUnit,
        String priceUnit,
        int maxCapacity,
        String imageUrl
) {}
