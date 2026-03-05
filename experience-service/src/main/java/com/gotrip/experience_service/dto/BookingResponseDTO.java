package com.gotrip.experience_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingResponseDTO(
        Long bookingId,
        Long experienceId,
        String experienceTitle,
        String experienceCategory,
        String experienceType,
        Long travellerId,
        Long providerId,
        LocalDate bookingDate,
        LocalTime startTime,
        int quantity,
        int durationHours,
        double totalPrice,
        String status,
        String providerMessage,
        String requestMessage,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        com.gotrip.common_library.dto.user.TravellerContactInfo contact) {}
