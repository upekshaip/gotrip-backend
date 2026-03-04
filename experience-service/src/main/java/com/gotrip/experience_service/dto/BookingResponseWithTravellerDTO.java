package com.gotrip.experience_service.dto;

import com.gotrip.common_library.dto.user.TravellerContactInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingResponseWithTravellerDTO(
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
        String declineReason,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        TravellerContactInfo travellerDetails // New field
) {}
