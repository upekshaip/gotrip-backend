package com.gotrip.experience_service.dto;

public record ProviderBookingDetailDTO(
        BookingResponseDTO booking,
        ExperienceResponseDTO experience
) {}